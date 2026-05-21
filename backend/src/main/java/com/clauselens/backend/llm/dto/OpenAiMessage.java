package com.clauselens.backend.llm.dto;

public record OpenAiMessage(
        String role,
        String content
) {
}