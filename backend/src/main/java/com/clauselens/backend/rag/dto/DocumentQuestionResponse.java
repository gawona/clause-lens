package com.clauselens.backend.rag.dto;

import java.util.List;

public record DocumentQuestionResponse(
        String question,
        String answer,
        List<RagEvidence> evidence
) {
}