package com.clauselens.backend.extraction.domain;

import com.clauselens.backend.document.domain.Document;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "document_extraction_results",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_document_extraction_result_document",
                        columnNames = "document_id"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DocumentExtractionResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contract_title")
    private String contractTitle;

    @Column(columnDefinition = "TEXT")
    private String partiesJson;

    @Column(name = "contract_amount")
    private String contractAmount;

    @Column(name = "contract_period")
    private String contractPeriod;

    @Column(name = "delivery_deadline")
    private String deliveryDeadline;

    @Column(columnDefinition = "TEXT")
    private String paymentTerms;

    @Column(columnDefinition = "TEXT")
    private String penaltyClause;

    @Column(columnDefinition = "TEXT")
    private String terminationClause;

    @Column(columnDefinition = "TEXT")
    private String evidenceJson;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public void update(
            String contractTitle,
            String partiesJson,
            String contractAmount,
            String contractPeriod,
            String deliveryDeadline,
            String paymentTerms,
            String penaltyClause,
            String terminationClause,
            String evidenceJson
    ) {
        this.contractTitle = contractTitle;
        this.partiesJson = partiesJson;
        this.contractAmount = contractAmount;
        this.contractPeriod = contractPeriod;
        this.deliveryDeadline = deliveryDeadline;
        this.paymentTerms = paymentTerms;
        this.penaltyClause = penaltyClause;
        this.terminationClause = terminationClause;
        this.evidenceJson = evidenceJson;
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }
}