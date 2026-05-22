package com.clauselens.backend.analysis.service;

import com.clauselens.backend.analysis.client.AnalysisEngineClient;
import com.clauselens.backend.analysis.dto.ExtractPageResponse;
import com.clauselens.backend.analysis.dto.ExtractRequest;
import com.clauselens.backend.analysis.dto.ExtractResponse;
import com.clauselens.backend.document.domain.Document;
import com.clauselens.backend.document.domain.DocumentPage;
import com.clauselens.backend.document.dto.DocumentResponse;
import com.clauselens.backend.document.repository.DocumentPageRepository;
import com.clauselens.backend.document.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentAnalysisService {

    private final DocumentRepository documentRepository;
    private final DocumentPageRepository documentPageRepository;
    private final AnalysisEngineClient analysisEngineClient;

    @Transactional
    public DocumentResponse analyzeDocument(UUID documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("문서를 찾을 수 없습니다. documentId=" + documentId));

        try {
            document.requestAnalysis();
            document.startExtracting();

            ExtractResponse extractResponse = analysisEngineClient.extractText(
                    new ExtractRequest(
                            document.getId(),
                            document.getFilePath()
                    )
            );

            documentPageRepository.deleteByDocument(document);
            documentPageRepository.flush();

            for (ExtractPageResponse page : extractResponse.pages()) {
                DocumentPage documentPage = DocumentPage.create(
                        document,
                        page.page(),
                        page.text()
                );

                documentPageRepository.save(documentPage);
            }

            return DocumentResponse.from(document);

        } catch (Exception e) {
            document.failAnalysis();
            throw new IllegalStateException("문서 분석 중 오류가 발생했습니다. 원인: " + e.getMessage(), e);
        }
    }
}