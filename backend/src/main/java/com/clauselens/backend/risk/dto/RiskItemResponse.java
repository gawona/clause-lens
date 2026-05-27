package com.clauselens.backend.risk.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RiskItemResponse(
        String riskLevel,
        String riskType,
        String reason,
        RiskEvidence evidence,
        String recommendation
) {
}