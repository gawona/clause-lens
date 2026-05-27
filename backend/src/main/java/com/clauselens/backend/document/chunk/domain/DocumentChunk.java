package com.clauselens.backend.document.chunk.domain;

import com.clauselens.backend.document.domain.Document;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "document_chunks",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_document_chunk_order",
                        columnNames = {"document_id", "chunk_order"}
                )
        },
        indexes = {
                @Index(name = "idx_document_chunks_document_id", columnList = "document_id"),
                @Index(name = "idx_document_chunks_page_number", columnList = "page_number"),
                @Index(name = "idx_document_chunks_chunk_type", columnList = "chunk_type")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DocumentChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chunk_order", nullable = false)
    private Integer chunkOrder;

    @Column(name = "page_number", nullable = false)
    private Integer pageNumber;

    @Column(name = "section_title")
    private String sectionTitle;

    @Enumerated(EnumType.STRING)
    @Column(name = "chunk_type", nullable = false)
    private ChunkType chunkType;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}