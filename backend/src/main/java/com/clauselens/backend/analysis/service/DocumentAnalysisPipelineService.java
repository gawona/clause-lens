package com.clauselens.backend.analysis.service;

import com.clauselens.backend.analysis.dto.DocumentAnalysisRunResponse;
import com.clauselens.backend.document.chunk.service.DocumentChunkingService;
import com.clauselens.backend.extraction.dto.ContractExtractionResponse;
import com.clauselens.backend.extraction.service.DocumentExtractionService;
import com.clauselens.backend.search.service.DocumentIndexService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentAnalysisPipelineService {

    private final DocumentAnalysisService documentAnalysisService;
    private final DocumentChunkingService documentChunkingService;
    private final DocumentIndexService documentIndexService;
    private final DocumentExtractionService documentExtractionService;

    public DocumentAnalysisRunResponse runAnalysis(UUID documentId) {
        documentAnalysisService.analyzeDocument(documentId);

        documentChunkingService.chunkDocument(documentId);

        documentIndexService.indexDocumentChunks(documentId);

        ContractExtractionResponse extraction = documentExtractionService.extractContractFields(documentId);

        return new DocumentAnalysisRunResponse(
                documentId,
                "문서 분석이 완료되었습니다.",
                extraction
        );
    }
}