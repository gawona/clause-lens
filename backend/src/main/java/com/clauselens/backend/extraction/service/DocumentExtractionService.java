package com.clauselens.backend.extraction.service;

import com.clauselens.backend.document.chunk.domain.DocumentChunk;
import com.clauselens.backend.document.chunk.repository.DocumentChunkRepository;
import com.clauselens.backend.document.domain.Document;
import com.clauselens.backend.document.repository.DocumentRepository;
import com.clauselens.backend.extraction.domain.DocumentExtractionResult;
import com.clauselens.backend.extraction.dto.ContractExtractionFields;
import com.clauselens.backend.extraction.dto.ContractExtractionResponse;
import com.clauselens.backend.extraction.dto.ExtractionEvidence;
import com.clauselens.backend.extraction.repository.DocumentExtractionResultRepository;
import com.clauselens.backend.llm.service.OpenAiChatClient;
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
public class DocumentExtractionService {

    private static final int TOP_K_PER_FIELD = 3;

    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final DocumentSearchService documentSearchService;
    private final OpenAiChatClient openAiChatClient;
    private final ObjectMapper objectMapper;
    private final DocumentExtractionResultRepository documentExtractionResultRepository;

    @Transactional
    public ContractExtractionResponse extractContractFields(UUID documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("문서를 찾을 수 없습니다. documentId=" + documentId));

        List<DocumentSearchResult> results = collectExtractionContext(documentId, document);

        if (results.isEmpty()) {
            return new ContractExtractionResponse(
                    null,
                    List.of(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    List.of()
            );
        }

        String systemPrompt = buildSystemPrompt();
        String userPrompt = buildUserPrompt(results);

        String llmContent = openAiChatClient.generateJson(systemPrompt, userPrompt);
        ContractExtractionFields fields = parseExtractionFields(llmContent);

        List<ExtractionEvidence> evidence = results.stream()
                .map(result -> new ExtractionEvidence(
                        result.chunkId(),
                        result.pageNumber(),
                        result.sectionTitle(),
                        result.content(),
                        result.score()
                ))
                .toList();

        ContractExtractionResponse response = new ContractExtractionResponse(
                fields.contractTitle(),
                fields.parties() == null ? List.of() : fields.parties(),
                fields.contractAmount(),
                fields.contractPeriod(),
                fields.deliveryDeadline(),
                fields.paymentTerms(),
                fields.penaltyClause(),
                fields.terminationClause(),
                evidence
        );

        saveExtractionResult(document, response);

        return response;
    }

    private List<DocumentSearchResult> collectExtractionContext(UUID documentId, Document document) {
        Map<Long, DocumentSearchResult> deduplicated = new LinkedHashMap<>();

        addFrontChunks(document, deduplicated);
        addFieldSearchResults(documentId, deduplicated);

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

    private void addFieldSearchResults(UUID documentId, Map<Long, DocumentSearchResult> deduplicated) {
        List<String> queries = List.of(
                "계약서 제목 계약명 계약 건명 물품구매계약",
                "계약담당자 계약상대자 상호 대표자 주소 농협경제지주 주식회사",
                "계약금액 총액 금액 부가가치세 공급가액 합계",
                "계약기간 계약 개시일 계약 종료일 유효기간",
                "납품기한 납품 기한 완료일 납품일 설치기한",
                "지급조건 대금 지급 청구 검사 검수 완료 잔금 선금 중도금",
                "지체상금 지체상금률 지연배상금 위약금 지체 일수",
                "계약 해지 해제 해약 계약 종료 위반"
        );

        for (String query : queries) {
            DocumentSearchResponse response = documentSearchService.search(documentId, query, TOP_K_PER_FIELD);

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
                당신은 계약서 문서에서 핵심 항목을 추출하는 AI 분석기입니다.

                규칙:
                1. 반드시 제공된 문서 조각에 있는 내용만 사용하세요.
                2. 문서 조각에서 찾을 수 없는 값은 null로 반환하세요.
                3. parties는 실제 계약 당사자만 배열로 반환하세요.
                4. "계약담당자", "계약상대자", "발주기관", "담당자" 같은 역할명만 있고 실제 회사명/기관명이 없으면 parties에는 넣지 마세요.
                5. 단, "농협경제지주", "주식회사", "법인명", "기관명"처럼 실제 조직명으로 확인되는 값은 parties에 포함할 수 있습니다.
                6. contractTitle은 문서 전체 제목 또는 계약서 본문 제목을 우선 사용하세요. 단순한 붙임 문서명이나 일반조건 제목만 있으면 null로 반환하세요.
                7. 계약금액은 숫자, 원화 표기, 총액, 부가가치세 포함 여부가 있으면 함께 적으세요.
                8. contractPeriod는 계약의 시작일과 종료일 또는 계약 효력 기간이 명확할 때만 추출하세요.
                9. 납품기한, 설치기한, 완료기한은 contractPeriod가 아니라 deliveryDeadline에 넣으세요.
                10. 문서에 계약기간은 없고 납품기한만 있으면 contractPeriod는 null, deliveryDeadline에는 해당 기한을 반환하세요.
                11. 추측하거나 일반적인 계약 관행으로 보완하지 마세요.
                12. 반드시 JSON 객체만 반환하세요.
                13. 마크다운 코드블록을 사용하지 마세요.
                14. 설명 문장을 추가하지 마세요.

                반환 JSON 형식:
                {
                  "contractTitle": string 또는 null,
                  "parties": string 배열,
                  "contractAmount": string 또는 null,
                  "contractPeriod": string 또는 null,
                  "deliveryDeadline": string 또는 null,
                  "paymentTerms": string 또는 null,
                  "penaltyClause": string 또는 null,
                  "terminationClause": string 또는 null
                }
                """;
    }

    private String buildUserPrompt(List<DocumentSearchResult> results) {
        String context = IntStream.range(0, results.size())
                .mapToObj(i -> formatContext(i + 1, results.get(i)))
                .reduce("", (a, b) -> a + b + "\n\n");

        return """
                아래 문서 조각에서 계약서 핵심 항목을 추출하세요.

                추출 기준:
                - contractTitle: 첫 페이지의 계약서 제목 우선. 일반조건/특수조건/물품명세서/견적서 제목은 제외.
                - parties: 첫 페이지의 계약담당자/계약상대자 영역에서 실제 상호명만 추출. 담당자명, 대표자명, 주소 제외.
                - contractAmount: 첫 페이지 계약금액 우선. 물품명세서/견적서 금액은 보조 근거.
                - contractPeriod: 계약 시작일~종료일 또는 계약 효력 기간만 추출. 없으면 null.
                - deliveryDeadline: 납품기한, 설치기한, 완료기한 추출.
                - paymentTerms: 계약 일반조건의 대가의 지급/청구/검사 합격 후 지급/14일 이내 지급 조항 우선. 견적서의 별도협의는 보조.
                - penaltyClause: 첫 페이지 지체상금률 우선, 일반조건의 지체상금 산정 방식으로 보완.
                - terminationClause: 해제/해지 조건을 핵심만 요약.

                참고:
                - 앞쪽 문서 조각은 계약명, 계약 당사자, 계약금액, 계약기간, 납품기한 판단에 우선 활용하세요.
                - 일반조건/특수조건 조각은 지급조건, 지체상금, 해지조건 판단에 주로 활용하세요.
                - 근거가 불명확하면 null을 반환하세요.

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
                result.content()
        );
    }

    private ContractExtractionFields parseExtractionFields(String llmContent) {
        try {
            String json = cleanJson(llmContent);
            return objectMapper.readValue(json, ContractExtractionFields.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("핵심 항목 추출 결과 JSON 파싱 중 오류가 발생했습니다. llmContent=" + llmContent, e);
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

    private void saveExtractionResult(Document document, ContractExtractionResponse response) {
        String partiesJson = writeJson(response.parties());
        String evidenceJson = writeJson(response.evidence());

        documentExtractionResultRepository.findByDocument(document)
                .ifPresentOrElse(
                        result -> result.update(
                                response.contractTitle(),
                                partiesJson,
                                response.contractAmount(),
                                response.contractPeriod(),
                                response.deliveryDeadline(),
                                response.paymentTerms(),
                                response.penaltyClause(),
                                response.terminationClause(),
                                evidenceJson
                        ),
                        () -> documentExtractionResultRepository.save(
                                DocumentExtractionResult.builder()
                                        .document(document)
                                        .contractTitle(response.contractTitle())
                                        .partiesJson(partiesJson)
                                        .contractAmount(response.contractAmount())
                                        .contractPeriod(response.contractPeriod())
                                        .deliveryDeadline(response.deliveryDeadline())
                                        .paymentTerms(response.paymentTerms())
                                        .penaltyClause(response.penaltyClause())
                                        .terminationClause(response.terminationClause())
                                        .evidenceJson(evidenceJson)
                                        .build()
                        )
                );
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("추출 결과 JSON 저장 문자열 변환 중 오류가 발생했습니다.", e);
        }
    }

    @Transactional(readOnly = true)
    public ContractExtractionResponse getContractExtractionResult(UUID documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("문서를 찾을 수 없습니다. documentId=" + documentId));

        DocumentExtractionResult result = documentExtractionResultRepository.findByDocument(document)
                .orElseThrow(() -> new IllegalStateException("저장된 핵심 항목 추출 결과가 없습니다. documentId=" + documentId));

        return new ContractExtractionResponse(
                result.getContractTitle(),
                readList(result.getPartiesJson(), String.class),
                result.getContractAmount(),
                result.getContractPeriod(),
                result.getDeliveryDeadline(),
                result.getPaymentTerms(),
                result.getPenaltyClause(),
                result.getTerminationClause(),
                readEvidenceList(result.getEvidenceJson())
        );
    }

    private <T> List<T> readList(String json, Class<T> elementType) {
        try {
            if (json == null || json.isBlank()) {
                return List.of();
            }

            return objectMapper.readValue(
                    json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, elementType)
            );
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("저장된 JSON 배열 파싱 중 오류가 발생했습니다.", e);
        }
    }

    private List<ExtractionEvidence> readEvidenceList(String json) {
        try {
            if (json == null || json.isBlank()) {
                return List.of();
            }

            return objectMapper.readValue(
                    json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, ExtractionEvidence.class)
            );
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("저장된 evidence JSON 파싱 중 오류가 발생했습니다.", e);
        }
    }
}