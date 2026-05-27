package com.clauselens.backend.risk.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RiskEvidence(
        Integer pageNumber,
        String sectionTitle,
        String text
) {
}