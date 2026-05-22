package com.clauselens.backend.analysis.dto;

import com.clauselens.backend.extraction.dto.ContractExtractionResponse;

import java.util.UUID;

public record DocumentAnalysisRunResponse(
        UUID documentId,
        String message,
        ContractExtractionResponse extraction
) {
}