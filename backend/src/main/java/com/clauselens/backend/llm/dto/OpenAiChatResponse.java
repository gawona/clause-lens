package com.clauselens.backend.llm.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenAiChatResponse(
        List<Choice> choices
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Choice(
            Message message
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Message(
            String role,
            String content
    ) {
    }

    public String firstContent() {
        if (choices == null || choices.isEmpty()) {
            return "";
        }

        Message message = choices.get(0).message();
        if (message == null || message.content() == null) {
            return "";
        }

        return message.content();
    }
}