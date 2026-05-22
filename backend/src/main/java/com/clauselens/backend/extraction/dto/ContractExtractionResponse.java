package com.clauselens.backend.extraction.dto;

import java.util.List;

public record ContractExtractionResponse(
        String contractTitle,
        List<String> parties,
        String contractAmount,
        String contractPeriod,
        String deliveryDeadline,
        String paymentTerms,
        String penaltyClause,
        String terminationClause,
        List<ExtractionEvidence> evidence
) {
}