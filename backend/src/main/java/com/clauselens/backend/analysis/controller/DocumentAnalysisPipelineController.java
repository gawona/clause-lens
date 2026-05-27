package com.clauselens.backend.analysis.controller;

import com.clauselens.backend.analysis.dto.DocumentAnalysisRunResponse;
import com.clauselens.backend.analysis.service.DocumentAnalysisPipelineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(
        name = "Document Analysis Pipeline",
        description = "문서 텍스트 추출, chunk 생성, OpenSearch 색인, 핵심 항목 추출을 한 번에 실행하는 API"
)
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentAnalysisPipelineController {

    private final DocumentAnalysisPipelineService documentAnalysisPipelineService;

    @Operation(
            summary = "문서 전체 분석 실행",
            description = """
                    지정한 문서에 대해 전체 분석 파이프라인을 실행합니다.

                    실행 순서:
                    1. FastAPI 분석엔진을 통한 PDF 텍스트 추출
                    2. 페이지별 텍스트 저장
                    3. 문서 chunk 생성
                    4. OpenSearch 색인
                    5. LLM 기반 핵심 항목 JSON 추출
                    6. 추출 결과 DB 저장
                    """
    )
    @PostMapping("/{documentId}/analysis/run")
    public DocumentAnalysisRunResponse runAnalysis(
            @Parameter(
                    description = "전체 분석을 실행할 문서 ID",
                    example = "6b630681-8409-4def-8e4f-2af22fd5ff9b",
                    required = true
            )
            @PathVariable UUID documentId
    ) {
        return documentAnalysisPipelineService.runAnalysis(documentId);
    }
}