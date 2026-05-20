package com.clauselens.backend.document.service;

import com.clauselens.backend.document.domain.Document;
import com.clauselens.backend.document.domain.DocumentPage;
import com.clauselens.backend.document.dto.DocumentPageResponse;
import com.clauselens.backend.document.dto.DocumentPagesResponse;
import com.clauselens.backend.document.dto.SaveDocumentPageRequest;
import com.clauselens.backend.document.dto.SaveDocumentPagesRequest;
import com.clauselens.backend.document.repository.DocumentPageRepository;
import com.clauselens.backend.document.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentPageService {

    private final DocumentRepository documentRepository;
    private final DocumentPageRepository documentPageRepository;

    @Transactional
    public DocumentPagesResponse savePages(
            UUID documentId,
            SaveDocumentPagesRequest request
    ) {
        Document document = getDocument(documentId);

        List<DocumentPageResponse> savedPages = request.pages()
                .stream()
                .sorted(Comparator.comparing(SaveDocumentPageRequest::page))
                .map(pageRequest -> saveOrUpdatePage(document, pageRequest))
                .map(DocumentPageResponse::from)
                .toList();

        return new DocumentPagesResponse(
                document.getId(),
                savedPages.size(),
                savedPages
        );
    }

    public DocumentPagesResponse getPages(UUID documentId) {
        Document document = getDocument(documentId);

        List<DocumentPageResponse> pages = documentPageRepository.findByDocumentOrderByPageNumberAsc(document)
                .stream()
                .map(DocumentPageResponse::from)
                .toList();

        return new DocumentPagesResponse(
                document.getId(),
                pages.size(),
                pages
        );
    }

    private DocumentPage saveOrUpdatePage(
            Document document,
            SaveDocumentPageRequest pageRequest
    ) {
        return documentPageRepository.findByDocumentAndPageNumber(document, pageRequest.page())
                .map(existingPage -> {
                    existingPage.updateContent(pageRequest.text());
                    return existingPage;
                })
                .orElseGet(() -> {
                    DocumentPage newPage = DocumentPage.create(
                            document,
                            pageRequest.page(),
                            pageRequest.text()
                    );

                    return documentPageRepository.save(newPage);
                });
    }

    private Document getDocument(UUID documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("문서를 찾을 수 없습니다. documentId=" + documentId));
    }
}