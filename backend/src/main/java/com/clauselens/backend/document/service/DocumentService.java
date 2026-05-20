package com.clauselens.backend.document.service;

import com.clauselens.backend.document.domain.Document;
import com.clauselens.backend.document.domain.DocumentType;
import com.clauselens.backend.document.dto.DocumentResponse;
import com.clauselens.backend.document.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public DocumentResponse uploadDocument(
            MultipartFile file,
            String documentName,
            DocumentType documentType
    ) {
        FileStorageService.StoredFile storedFile = fileStorageService.storePdf(file);

        String originalFilename = file.getOriginalFilename();
        String resolvedDocumentName = resolveDocumentName(documentName, originalFilename);

        Document document = Document.create(
                resolvedDocumentName,
                originalFilename,
                storedFile.storedFilename(),
                storedFile.filePath(),
                resolveContentType(file.getContentType()),
                file.getSize(),
                documentType
        );

        Document savedDocument = documentRepository.save(document);

        return DocumentResponse.from(savedDocument);
    }

    public List<DocumentResponse> getDocuments() {
        return documentRepository.findAll()
                .stream()
                .map(DocumentResponse::from)
                .toList();
    }

    public DocumentResponse getDocument(UUID documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("문서를 찾을 수 없습니다. documentId=" + documentId));

        return DocumentResponse.from(document);
    }

    @Transactional
    public DocumentResponse requestAnalysis(UUID documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("문서를 찾을 수 없습니다. documentId=" + documentId));

        document.requestAnalysis();

        return DocumentResponse.from(document);
    }

    private String resolveDocumentName(String documentName, String originalFilename) {
        if (documentName != null && !documentName.isBlank()) {
            return documentName;
        }

        if (originalFilename == null || originalFilename.isBlank()) {
            return "untitled";
        }

        return originalFilename.replaceFirst("(?i)\\.pdf$", "");
    }

    private String resolveContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return "application/pdf";
        }

        return contentType;
    }
}