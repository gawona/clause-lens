package com.clauselens.backend.search.controller;

import com.clauselens.backend.search.service.DocumentIndexService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(
        name = "Document Index",
        description = "문서 chunk 데이터를 OpenSearch에 색인하는 API"
)
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentIndexController {

    private final DocumentIndexService documentIndexService;

    @Operation(
            summary = "문서 chunk OpenSearch 색인",
            description = """
                    지정한 문서의 chunk 데이터를 조회하여 OpenSearch에 색인합니다.

                    처리 순서:
                    1. documentId로 문서 조회
                    2. document_chunks 테이블에서 chunk 목록 조회
                    3. OpenSearch index 존재 여부 확인
                    4. index가 없으면 생성
                    5. bulk API로 chunk 데이터 색인

                    이 API는 RAG 검색을 위한 사전 단계입니다.
                    """
    )
    @PostMapping("/{documentId}/index")
    public ResponseEntity<Void> indexDocumentChunks(
            @Parameter(
                    description = "OpenSearch에 색인할 문서 ID",
                    example = "6b630681-8409-4def-8e4f-2af22fd5ff9b",
                    required = true
            )
            @PathVariable UUID documentId
    ) {
        documentIndexService.indexDocumentChunks(documentId);
        return ResponseEntity.ok().build();
    }
}