package com.clauselens.backend.rag.controller;

import com.clauselens.backend.rag.dto.DocumentQuestionRequest;
import com.clauselens.backend.rag.dto.DocumentQuestionResponse;
import com.clauselens.backend.rag.service.DocumentRagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(
        name = "Document RAG",
        description = "OpenSearch 검색 결과와 LLM을 결합하여 문서 기반 질의응답을 수행하는 API"
)
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentQuestionController {

    private final DocumentRagService documentRagService;

    @Operation(
            summary = "문서 기반 질의응답",
            description = """
                    지정한 문서에 대해 질문하면 OpenSearch에서 관련 chunk를 검색하고,
                    검색된 근거 chunk를 LLM prompt에 포함하여 답변을 생성합니다.

                    처리 순서:
                    1. documentId 기준으로 검색 범위 제한
                    2. 질문과 관련 있는 chunk top-k 검색
                    3. 검색 결과를 prompt context로 구성
                    4. LLM 호출
                    5. 답변과 근거 chunk 반환
                    """
    )
    @PostMapping("/{documentId}/questions")
    public DocumentQuestionResponse ask(
            @Parameter(
                    description = "질문 대상 문서 ID",
                    example = "6b630681-8409-4def-8e4f-2af22fd5ff9b",
                    required = true
            )
            @PathVariable UUID documentId,

            @RequestBody DocumentQuestionRequest request
    ) {
        return documentRagService.ask(documentId, request.question());
    }
}