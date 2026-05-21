package com.clauselens.backend.llm.dto;

import java.util.List;

public record OpenAiChatRequest(
        String model,
        List<OpenAiMessage> messages,
        Double temperature
) {
}