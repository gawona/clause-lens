package com.clauselens.backend.document.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "documents")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String documentName;

    @Column(nullable = false, length = 255)
    private String originalFilename;

    @Column(nullable = false, length = 255)
    private String storedFilename;

    @Column(nullable = false, length = 500)
    private String filePath;

    @Column(nullable = false, length = 100)
    private String contentType;

    @Column(nullable = false)
    private Long fileSize;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private DocumentType documentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private UploadStatus uploadStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AnalysisStatus analysisStatus;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private Document(
            String documentName,
            String originalFilename,
            String storedFilename,
            String filePath,
            String contentType,
            Long fileSize,
            DocumentType documentType
    ) {
        this.documentName = documentName;
        this.originalFilename = originalFilename;
        this.storedFilename = storedFilename;
        this.filePath = filePath;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.documentType = documentType;
        this.uploadStatus = UploadStatus.UPLOADED;
        this.analysisStatus = AnalysisStatus.NOT_STARTED;
    }

    public static Document create(
            String documentName,
            String originalFilename,
            String storedFilename,
            String filePath,
            String contentType,
            Long fileSize,
            DocumentType documentType
    ) {
        return new Document(
                documentName,
                originalFilename,
                storedFilename,
                filePath,
                contentType,
                fileSize,
                documentType
        );
    }

    public void requestAnalysis() {
        this.analysisStatus = AnalysisStatus.REQUESTED;
    }

    public void failUpload() {
        this.uploadStatus = UploadStatus.FAILED;
    }

    public void failAnalysis() {
        this.analysisStatus = AnalysisStatus.FAILED;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}