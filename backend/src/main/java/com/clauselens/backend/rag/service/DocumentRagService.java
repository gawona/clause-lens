package com.clauselens.backend.rag.service;

import com.clauselens.backend.llm.service.OpenAiChatClient;
import com.clauselens.backend.rag.dto.DocumentQuestionResponse;
import com.clauselens.backend.rag.dto.RagEvidence;
import com.clauselens.backend.search.dto.DocumentSearchResponse;
import com.clauselens.backend.search.dto.DocumentSearchResult;
import com.clauselens.backend.search.service.DocumentSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class DocumentRagService {

    private static final int TOP_K = 5;

    private final DocumentSearchService documentSearchService;
    private final OpenAiChatClient openAiChatClient;

    public DocumentQuestionResponse ask(UUID documentId, String question) {
        DocumentSearchResponse searchResponse = documentSearchService.search(documentId, question, TOP_K);
        List<DocumentSearchResult> results = searchResponse.results();

        if (results.isEmpty()) {
            return new DocumentQuestionResponse(
                    question,
                    "문서에서 명확한 근거를 찾을 수 없습니다.",
                    List.of()
            );
        }

        String systemPrompt = buildSystemPrompt();
        String userPrompt = buildUserPrompt(question, results);

        String answer = openAiChatClient.generateAnswer(systemPrompt, userPrompt);

        List<RagEvidence> evidence = results.stream()
                .map(result -> new RagEvidence(
                        result.chunkId(),
                        result.pageNumber(),
                        result.sectionTitle(),
                        result.content(),
                        result.score()
                ))
                .toList();

        return new DocumentQuestionResponse(question, answer, evidence);
    }

    private String buildSystemPrompt() {
        return """
                당신은 계약서, 약관, 공고문, 정책 문서, 일반 업무 문서를 분석하는 AI 문서 분석 도우미입니다.

                규칙:
                1. 반드시 제공된 근거 문서 조각만 사용해서 답변하세요.
                2. 근거에 없는 내용은 추측하지 마세요.
                3. 근거가 부족하면 "문서에서 명확한 근거를 찾을 수 없습니다."라고 답변하세요.
                4. 답변은 한국어로 작성하세요.
                5. 가능한 경우 페이지 번호와 조항명을 언급하세요.
                """;
    }

    private String buildUserPrompt(String question, List<DocumentSearchResult> results) {
        String context = IntStream.range(0, results.size())
                .mapToObj(i -> formatContext(i + 1, results.get(i)))
                .reduce("", (a, b) -> a + b + "\n\n");

        return """
                사용자 질문:
                %s

                검색된 근거 문서 조각:
                %s

                위 근거만 사용해서 답변하세요.
                """.formatted(question, context);
    }

    private String formatContext(int index, DocumentSearchResult result) {
        return """
                [근거 %d]
                page: %d
                section: %s
                score: %.4f
                text:
                %s
                """.formatted(
                index,
                result.pageNumber(),
                result.sectionTitle() == null ? "" : result.sectionTitle(),
                result.score(),
                result.content()
        );
    }
}