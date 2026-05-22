package com.clauselens.backend.extraction.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ContractExtractionFields(
        String contractTitle,
        List<String> parties,
        String contractAmount,
        String contractPeriod,
        String deliveryDeadline,
        String paymentTerms,
        String penaltyClause,
        String terminationClause
) {
}