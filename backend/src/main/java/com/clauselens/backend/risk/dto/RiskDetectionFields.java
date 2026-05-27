package com.clauselens.backend.risk.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RiskDetectionFields(
        List<RiskItemResponse> risks
) {
}