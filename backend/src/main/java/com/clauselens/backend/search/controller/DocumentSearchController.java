package com.clauselens.backend.search.controller;

import com.clauselens.backend.search.dto.DocumentSearchResponse;
import com.clauselens.backend.search.service.DocumentSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(
        name = "Document Search",
        description = "OpenSearch에 색인된 문서 chunk를 검색하는 API"
)
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentSearchController {

    private final DocumentSearchService documentSearchService;

    @Operation(
            summary = "문서 chunk 검색",
            description = """
                    지정한 문서 ID에 해당하는 chunk 중에서 검색어와 관련 있는 chunk를 조회합니다.

                    처리 순서:
                    1. documentId로 검색 범위 제한
                    2. content, sectionTitle 필드를 대상으로 전문 검색
                    3. OpenSearch score 기준으로 관련 chunk 반환

                    이 API는 이후 RAG 질의응답에서 관련 근거 chunk를 찾는 데 사용됩니다.
                    """
    )
    @GetMapping("/{documentId}/search")
    public DocumentSearchResponse search(
            @Parameter(
                    description = "검색 대상 문서 ID",
                    example = "6b630681-8409-4def-8e4f-2af22fd5ff9b",
                    required = true
            )
            @PathVariable UUID documentId,

            @Parameter(
                    description = "검색어",
                    example = "지체상금",
                    required = true
            )
            @RequestParam String query
    ) {
        return documentSearchService.search(documentId, query);
    }
}