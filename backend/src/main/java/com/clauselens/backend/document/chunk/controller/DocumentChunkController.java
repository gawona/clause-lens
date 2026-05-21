package com.clauselens.backend.document.chunk.controller;

import com.clauselens.backend.document.chunk.service.DocumentChunkingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(
        name = "Document Chunk",
        description = "문서 페이지 텍스트를 검색 가능한 chunk 단위로 분리하고 저장하는 API"
)
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentChunkController {

    private final DocumentChunkingService documentChunkingService;

    @Operation(
            summary = "문서 Chunking 실행",
            description = """
                    지정한 문서의 페이지별 텍스트(DocumentPage)를 조회하여 검색 가능한 chunk 단위로 분리합니다.

                    처리 순서:
                    1. documentId로 문서 조회
                    2. 해당 문서의 페이지 텍스트 조회
                    3. 기존 chunk 삭제
                    4. 조항/문단/길이 기준으로 chunk 생성
                    5. document_chunks 테이블에 저장

                    이 API는 OpenSearch 색인 전 단계에서 사용됩니다.
                    개발 초기에는 수동 테스트용으로 호출하고, 이후에는 문서 분석 완료 흐름에서 자동 호출됩니다.
                    """
    )
    @PostMapping("/{documentId}/chunks")
    public ResponseEntity<Void> chunkDocument(
            @Parameter(
                    description = "Chunking을 실행할 문서 ID",
                    example = "6b630681-8409-4def-8e4f-2af22fd5ff9b",
                    required = true
            )
            @PathVariable UUID documentId
    ) {
        documentChunkingService.chunkDocument(documentId);
        return ResponseEntity.ok().build();
    }
}