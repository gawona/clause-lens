package com.clauselens.backend.document.dto;

import com.clauselens.backend.document.domain.AnalysisStatus;
import com.clauselens.backend.document.domain.Document;
import com.clauselens.backend.document.domain.DocumentType;
import com.clauselens.backend.document.domain.UploadStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record DocumentResponse(
        UUID documentId,
        String documentName,
        String originalFilename,
        Long fileSize,
        String contentType,
        DocumentType documentType,
        UploadStatus uploadStatus,
        AnalysisStatus analysisStatus,
        LocalDateTime createdAt
) {

    public static DocumentResponse from(Document document) {
        return new DocumentResponse(
                document.getId(),
                document.getDocumentName(),
                document.getOriginalFilename(),
                document.getFileSize(),
                document.getContentType(),
                document.getDocumentType(),
                document.getUploadStatus(),
                document.getAnalysisStatus(),
                document.getCreatedAt()
        );
    }
}