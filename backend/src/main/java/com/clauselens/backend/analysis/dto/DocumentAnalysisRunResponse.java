package com.clauselens.backend.analysis.dto;

import com.clauselens.backend.extraction.dto.ContractExtractionResponse;
import com.clauselens.backend.risk.dto.RiskDetectionResponse;

import java.util.UUID;

public record DocumentAnalysisRunResponse(
        UUID documentId,
        String message,
        ContractExtractionResponse extraction,
        RiskDetectionResponse risks
) {
}