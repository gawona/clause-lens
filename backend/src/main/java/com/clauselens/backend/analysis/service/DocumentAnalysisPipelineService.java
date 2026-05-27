package com.clauselens.backend.analysis.service;

import com.clauselens.backend.analysis.dto.DocumentAnalysisRunResponse;
import com.clauselens.backend.document.chunk.service.DocumentChunkingService;
import com.clauselens.backend.document.domain.Document;
import com.clauselens.backend.document.service.DocumentStatusService;
import com.clauselens.backend.extraction.dto.ContractExtractionResponse;
import com.clauselens.backend.extraction.service.DocumentExtractionService;
import com.clauselens.backend.risk.dto.RiskDetectionResponse;
import com.clauselens.backend.risk.service.DocumentRiskService;
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
    private final DocumentRiskService documentRiskService;
    private final DocumentStatusService documentStatusService;

    public DocumentAnalysisRunResponse runAnalysis(UUID documentId) {
        try {
            documentAnalysisService.analyzeDocument(documentId);

            documentStatusService.updateStatus(documentId, Document::startChunking);
            documentChunkingService.chunkDocument(documentId);

            documentStatusService.updateStatus(documentId, Document::startIndexing);
            documentIndexService.indexDocumentChunks(documentId);

            ContractExtractionResponse extraction =
                    documentExtractionService.extractContractFields(documentId);

            RiskDetectionResponse risks =
                    documentRiskService.detectRisks(documentId);

            documentStatusService.updateStatus(documentId, Document::completeAnalysis);

            return new DocumentAnalysisRunResponse(
                    documentId,
                    "문서 분석이 완료되었습니다.",
                    extraction,
                    risks
            );
        } catch (Exception e) {
            documentStatusService.updateStatus(documentId, Document::failAnalysis);
            throw e;
        }
    }
}