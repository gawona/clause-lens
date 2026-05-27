package com.clauselens.backend.document.controller;

import com.clauselens.backend.analysis.service.DocumentAnalysisService;
import com.clauselens.backend.document.domain.DocumentType;
import com.clauselens.backend.document.dto.DocumentResponse;
import com.clauselens.backend.document.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Tag(name = "Document", description = "문서 업로드 및 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;
    private final DocumentAnalysisService documentAnalysisService;

    @Operation(summary = "PDF 문서 업로드", description = "PDF 파일과 문서 메타데이터를 업로드합니다.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentResponse uploadDocument(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "documentName", required = false) String documentName,
            @RequestParam(value = "documentType", defaultValue = "BUSINESS_DOCUMENT") DocumentType documentType
    ) {
        return documentService.uploadDocument(file, documentName, documentType);
    }

    @Operation(summary = "문서 목록 조회", description = "업로드된 문서 목록을 조회합니다.")
    @GetMapping
    public List<DocumentResponse> getDocuments() {
        return documentService.getDocuments();
    }

    @Operation(summary = "문서 상세 조회", description = "documentId로 문서 상세 정보를 조회합니다.")
    @GetMapping("/{documentId}")
    public DocumentResponse getDocument(
            @PathVariable UUID documentId
    ) {
        return documentService.getDocument(documentId);
    }

    @Operation(summary = "문서 분석 요청", description = "FastAPI 분석엔진을 호출하여 PDF 텍스트를 추출하고 페이지별 텍스트를 저장합니다.")
    @PostMapping("/{documentId}/analysis")
    public DocumentResponse requestAnalysis(
            @PathVariable UUID documentId
    ) {
        return documentAnalysisService.analyzeDocument(documentId);
    }
}