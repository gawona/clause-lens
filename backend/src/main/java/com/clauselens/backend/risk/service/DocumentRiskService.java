package com.clauselens.backend.risk.service;

import com.clauselens.backend.document.chunk.domain.DocumentChunk;
import com.clauselens.backend.document.chunk.repository.DocumentChunkRepository;
import com.clauselens.backend.document.domain.Document;
import com.clauselens.backend.document.repository.DocumentRepository;
import com.clauselens.backend.llm.service.OpenAiChatClient;
import com.clauselens.backend.risk.domain.DocumentRiskResult;
import com.clauselens.backend.risk.dto.RiskDetectionFields;
import com.clauselens.backend.risk.dto.RiskDetectionResponse;
import com.clauselens.backend.risk.dto.RiskItemResponse;
import com.clauselens.backend.risk.repository.DocumentRiskResultRepository;
import com.clauselens.backend.search.dto.DocumentSearchResponse;
import com.clauselens.backend.search.dto.DocumentSearchResult;
import com.clauselens.backend.search.service.DocumentSearchService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class DocumentRiskService {

    private static final int TOP_K_PER_RISK = 2;
    private static final int MAX_CONTEXT_CONTENT_LENGTH = 1800;

    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final DocumentSearchService documentSearchService;
    private final OpenAiChatClient openAiChatClient;
    private final ObjectMapper objectMapper;
    private final DocumentRiskResultRepository documentRiskResultRepository;

    @Transactional
    public RiskDetectionResponse detectRisks(UUID documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("문서를 찾을 수 없습니다. documentId=" + documentId));

        List<DocumentSearchResult> results = collectRiskContext(documentId, document);

        if (results.isEmpty()) {
            RiskDetectionResponse emptyResponse = new RiskDetectionResponse(documentId, List.of());
            saveRiskResult(document, emptyResponse);
            return emptyResponse;
        }

        String systemPrompt = buildSystemPrompt();
        String userPrompt = buildUserPrompt(results);

        String llmContent = openAiChatClient.generateJson(systemPrompt, userPrompt);
        RiskDetectionFields fields = parseRiskDetectionFields(llmContent);

        RiskDetectionResponse response = new RiskDetectionResponse(
                documentId,
                fields.risks() == null ? List.of() : fields.risks()
        );

        saveRiskResult(document, response);

        return response;
    }

    @Transactional(readOnly = true)
    public RiskDetectionResponse getRiskResult(UUID documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("문서를 찾을 수 없습니다. documentId=" + documentId));

        DocumentRiskResult result = documentRiskResultRepository.findByDocument(document)
                .orElseThrow(() -> new IllegalStateException("저장된 위험 조항 탐지 결과가 없습니다. documentId=" + documentId));

        return new RiskDetectionResponse(
                documentId,
                readRiskList(result.getRisksJson())
        );
    }

    private void saveRiskResult(Document document, RiskDetectionResponse response) {
        String risksJson = writeJson(response.risks());

        documentRiskResultRepository.findByDocument(document)
                .ifPresentOrElse(
                        result -> result.update(risksJson),
                        () -> documentRiskResultRepository.save(
                                DocumentRiskResult.builder()
                                        .document(document)
                                        .risksJson(risksJson)
                                        .build()
                        )
                );
    }

    private List<DocumentSearchResult> collectRiskContext(UUID documentId, Document document) {
        Map<Long, DocumentSearchResult> deduplicated = new LinkedHashMap<>();

        addFrontChunks(document, deduplicated);
        addRiskSearchResults(documentId, deduplicated);

        return deduplicated.values()
                .stream()
                .sorted(Comparator
                        .comparing(DocumentSearchResult::pageNumber)
                        .thenComparing(DocumentSearchResult::chunkOrder))
                .toList();
    }

    private void addFrontChunks(Document document, Map<Long, DocumentSearchResult> deduplicated) {
        List<DocumentChunk> frontChunks = documentChunkRepository
                .findTop5ByDocumentOrderByPageNumberAscChunkOrderAsc(document);

        for (DocumentChunk chunk : frontChunks) {
            deduplicated.putIfAbsent(chunk.getId(), toSearchResult(chunk));
        }
    }

    private void addRiskSearchResults(UUID documentId, Map<Long, DocumentSearchResult> deduplicated) {
        List<String> queries = List.of(
                "계약금액 계약 금액 총액 부가세 공급가액",
                "계약기간 계약 개시일 계약 종료일 유효기간 납품기한",
                "검사 검수 합격 불합격 검사기간 검수기준 인수확인",
                "지체상금 지체상금률 지연배상금 위약금 손해배상",
                "계약 해제 해지 계약 종료 위반 귀책사유",
                "대가의 지급 대금 지급 청구 지급기한 결제조건",
                "비밀유지 보안 개인정보 위탁 개인정보처리",
                "하도급 양도 권리의무 양도 승인",
                "저작권 지식재산권 특허권 소스코드 결과물",
                "손해배상 책임 제한 면책 배상 예정액",
                "무상유지보수 하자보수 보증기간 하자보수기간 공란"
        );

        for (String query : queries) {
            DocumentSearchResponse response = documentSearchService.search(documentId, query, TOP_K_PER_RISK);

            for (DocumentSearchResult result : response.results()) {
                deduplicated.putIfAbsent(result.chunkId(), result);
            }
        }
    }

    private DocumentSearchResult toSearchResult(DocumentChunk chunk) {
        return new DocumentSearchResult(
                chunk.getId(),
                chunk.getDocument().getId().toString(),
                chunk.getPageNumber(),
                chunk.getChunkOrder(),
                chunk.getSectionTitle(),
                chunk.getChunkType().name(),
                chunk.getContent(),
                0.0
        );
    }

    private String buildSystemPrompt() {
        return """
                당신은 계약서·약관·정책 문서의 위험 조항을 검토하는 AI 분석기입니다.

                기본 규칙:
                1. 반드시 제공된 문서 조각만 근거로 판단하세요.
                2. 문서에 없는 내용은 추측하지 마세요.
                3. 과도하게 일반적인 위험은 만들지 마세요.
                4. 같은 위험은 중복 반환하지 마세요.
                5. 반드시 JSON 객체만 반환하세요.
                6. 마크다운 코드블록과 설명 문장은 출력하지 마세요.

                과탐지 방지 규칙:
                1. 문서에 이미 조건, 기준, 예외, 절차가 구체적으로 명시된 항목은 위험으로 판단하지 마세요.
                2. "제공된 조각에서 확인되지 않는다"는 이유만으로 위험을 만들지 마세요.
                3. 단순히 조항이 길거나 엄격하다는 이유만으로 위험으로 판단하지 마세요.
                4. 계약서 일반조건에 흔히 있는 표준 조항은, 책임 범위가 과도하거나 핵심 값이 비어 있을 때만 위험으로 판단하세요.
                5. 계약금액, 납품기한, 지급조건처럼 문서에서 명확히 확인되는 항목은 누락 위험으로 판단하지 마세요.
                6. 지체상금률, 산정 방식, 면책 사유가 함께 확인되면 "지체상금 불명확" 위험은 제외하세요.
                7. 개인정보 처리 조항은 실제 개인정보 위탁 처리 가능성이 문서에서 확인될 때만 위험으로 판단하세요.
                   단순히 개인정보처리 위탁계약 체결 의무가 있다는 이유만으로 HIGH로 판단하지 마세요.
                8. 위험 판단은 계약상대자에게 실질적인 법적·금전적·운영상 불리함이 있는 경우에 한정하세요.

                위험 수준 기준:
                - HIGH: 손해배상, 해지, 보안, 개인정보, 지식재산권, 금전 책임에서 명확하고 큰 리스크가 있음
                - MEDIUM: 조건이 존재하지만 범위가 넓거나 핵심 기준이 모호하여 분쟁 가능성이 있음
                - LOW: 주의 수준의 리스크 또는 보완하면 좋은 사항

                탐지 대상 예시:
                - 계약금액 없음
                - 계약기간 또는 납품기한 불명확
                - 검수 기준 또는 검사 기간 불명확
                - 지체상금 또는 위약금 과도
                - 해지 조건 불명확 또는 일방에게 불리
                - 손해배상 범위 과도
                - 개인정보 처리 또는 보안 조항 미흡
                - 저작권, 지식재산권 귀속 불명확
                - 하도급, 권리의무 양도 조건 불명확
                - 지급 조건 불명확
                - 무상유지보수기간 또는 하자보수기간 공란

                반환 JSON 형식:
                {
                  "risks": [
                    {
                      "riskLevel": "HIGH" 또는 "MEDIUM" 또는 "LOW",
                      "riskType": string,
                      "reason": string,
                      "evidence": {
                        "pageNumber": number,
                        "sectionTitle": string 또는 null,
                        "text": string
                      },
                      "recommendation": string
                    }
                  ]
                }
                """;
    }

    private String buildUserPrompt(List<DocumentSearchResult> results) {
        String context = IntStream.range(0, results.size())
                .mapToObj(i -> formatContext(i + 1, results.get(i)))
                .reduce("", (a, b) -> a + b + "\n\n");

        return """
                아래 문서 조각을 기반으로 위험 조항과 누락 가능성이 있는 항목을 탐지하세요.

                판단 규칙:
                - 근거 문장이 있는 위험만 반환하세요.
                - 같은 위험은 중복 반환하지 마세요.
                - 최대 6개까지만 중요도 순으로 반환하세요.
                - evidence.text에는 실제 근거 문장을 짧게 넣으세요.
                - recommendation에는 실무적으로 보완할 문구 방향을 제안하세요.
                - 단정적인 표현보다 문서 근거에 맞는 수준으로 표현하세요.

                제외 규칙:
                - 계약금액이 확인되면 "계약금액 없음" 위험은 제외하세요.
                - 납품기한이 확인되면 "납품기한 없음" 위험은 제외하세요.
                - 지급조건이 확인되면 "지급조건 없음" 위험은 제외하세요.
                - 지체상금률과 산정 방식이 확인되면 "지체상금 불명확" 위험은 제외하세요.
                - 개인정보처리 위탁계약 체결 의무만으로는 위험으로 판단하지 마세요.
                - 단순히 "추가 확인 필요" 수준의 내용은 위험으로 반환하지 마세요.

                우선 탐지할 만한 항목:
                - 손해배상 범위가 직접손해/간접손해/상한 없이 넓게 규정된 경우
                - 계약보증금 귀속과 별도 손해배상청구가 중복될 수 있는 경우
                - 무상유지보수기간, 하자보수기간 등 핵심 기간이 공란인 경우
                - 하도급 업체의 보안관리 책임까지 포괄적으로 부담하는 경우
                - 개인정보 또는 보안 위반 책임 범위가 과도하게 넓은 경우
                - 지식재산권 또는 산출물 귀속이 불명확한 경우

                문서 조각:
                %s
                """.formatted(context);
    }

    private String formatContext(int index, DocumentSearchResult result) {
        return """
                [문서 조각 %d]
                chunkId: %d
                page: %d
                section: %s
                score: %.4f
                text:
                %s
                """.formatted(
                index,
                result.chunkId(),
                result.pageNumber(),
                result.sectionTitle() == null ? "" : result.sectionTitle(),
                result.score(),
                truncate(result.content(), MAX_CONTEXT_CONTENT_LENGTH)
        );
    }

    private String truncate(String text, int maxLength) {
        if (text == null) {
            return "";
        }

        if (text.length() <= maxLength) {
            return text;
        }

        return text.substring(0, maxLength) + "...";
    }

    private RiskDetectionFields parseRiskDetectionFields(String llmContent) {
        try {
            String json = cleanJson(llmContent);
            return objectMapper.readValue(json, RiskDetectionFields.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("위험 조항 탐지 결과 JSON 파싱 중 오류가 발생했습니다. llmContent=" + llmContent, e);
        }
    }

    private String cleanJson(String content) {
        if (content == null) {
            return "{}";
        }

        return content
                .replace("```json", "")
                .replace("```", "")
                .trim();
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("위험 조항 탐지 결과 JSON 저장 문자열 변환 중 오류가 발생했습니다.", e);
        }
    }

    private List<RiskItemResponse> readRiskList(String json) {
        try {
            if (json == null || json.isBlank()) {
                return List.of();
            }

            return objectMapper.readValue(
                    json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, RiskItemResponse.class)
            );
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("저장된 위험 조항 JSON 파싱 중 오류가 발생했습니다.", e);
        }
    }
}