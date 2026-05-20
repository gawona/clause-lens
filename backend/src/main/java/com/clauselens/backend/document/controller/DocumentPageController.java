package com.clauselens.backend.document.controller;

import com.clauselens.backend.document.dto.DocumentPagesResponse;
import com.clauselens.backend.document.dto.SaveDocumentPagesRequest;
import com.clauselens.backend.document.service.DocumentPageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Document Page", description = "문서 페이지별 텍스트 저장 및 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/documents/{documentId}/pages")
public class DocumentPageController {

    private final DocumentPageService documentPageService;

    @Operation(
            summary = "문서 페이지별 텍스트 수동 저장",
            description = "FastAPI 추출 결과를 수동으로 저장하기 위한 개발/디버깅용 API입니다. 일반 분석 흐름에서는 /analysis API가 자동으로 저장합니다."
    )
    @PostMapping
    public DocumentPagesResponse savePages(
            @PathVariable UUID documentId,
            @Valid @RequestBody SaveDocumentPagesRequest request
    ) {
        return documentPageService.savePages(documentId, request);
    }

    @Operation(
            summary = "문서 페이지별 텍스트 조회",
            description = "문서 ID 기준으로 저장된 페이지별 텍스트를 조회합니다."
    )
    @GetMapping
    public DocumentPagesResponse getPages(
            @PathVariable UUID documentId
    ) {
        return documentPageService.getPages(documentId);
    }
}