package com.clauselens.backend.document.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "document_pages",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_document_page",
                        columnNames = {"document_id", "page_number"}
                )
        },
        indexes = {
                @Index(name = "idx_document_pages_document_id", columnList = "document_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DocumentPage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(name = "page_number", nullable = false)
    private Integer pageNumber;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private DocumentPage(
            Document document,
            Integer pageNumber,
            String content
    ) {
        this.document = document;
        this.pageNumber = pageNumber;
        this.content = content;
    }

    public static DocumentPage create(
            Document document,
            Integer pageNumber,
            String content
    ) {
        return new DocumentPage(document, pageNumber, content);
    }

    public void updateContent(String content) {
        this.content = content;
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