package com.clauselens.backend.risk.dto;

import java.util.List;
import java.util.UUID;

public record RiskDetectionResponse(
        UUID documentId,
        List<RiskItemResponse> risks
) {
}