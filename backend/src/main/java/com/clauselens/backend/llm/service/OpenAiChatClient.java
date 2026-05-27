package com.clauselens.backend.llm.service;

import com.clauselens.backend.llm.config.OpenAiProperties;
import com.clauselens.backend.llm.dto.OpenAiChatRequest;
import com.clauselens.backend.llm.dto.OpenAiChatResponse;
import com.clauselens.backend.llm.dto.OpenAiMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OpenAiChatClient {

    @Qualifier("openAiRestClient")
    private final RestClient openAiRestClient;

    private final OpenAiProperties openAiProperties;
    private final ObjectMapper objectMapper;

    public String generateAnswer(String systemPrompt, String userPrompt) {
        System.out.println("OpenAI model = " + openAiProperties.getModel());
        System.out.println("OpenAI key loaded = " +
                (openAiProperties.getApiKey() != null && openAiProperties.getApiKey().startsWith("sk-")));

        OpenAiChatRequest request = new OpenAiChatRequest(
                openAiProperties.getModel(),
                List.of(
                        new OpenAiMessage("system", systemPrompt),
                        new OpenAiMessage("user", userPrompt)
                ),
                0.2
        );

        return openAiRestClient.post()
                .uri("/chat/completions")
                .body(request)
                .exchange((clientRequest, clientResponse) -> {
                    String responseBody = readBody(clientResponse.getBody());
                    HttpStatusCode statusCode = clientResponse.getStatusCode();

                    System.out.println("OpenAI status = " + statusCode);
                    System.out.println("OpenAI response body = " + responseBody);

                    if (!statusCode.is2xxSuccessful()) {
                        throw new IllegalStateException("OpenAI API 호출 실패: " + responseBody);
                    }

                    OpenAiChatResponse response = objectMapper.readValue(responseBody, OpenAiChatResponse.class);
                    return response.firstContent();
                });
    }

    private String readBody(java.io.InputStream body) {
        try {
            return StreamUtils.copyToString(body, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("OpenAI 응답 body 읽기 실패", e);
        }
    }

    public String generateJson(String systemPrompt, String userPrompt) {
        OpenAiChatRequest request = new OpenAiChatRequest(
                openAiProperties.getModel(),
                List.of(
                        new OpenAiMessage("system", systemPrompt),
                        new OpenAiMessage("user", userPrompt)
                ),
                0.0
        );

        return openAiRestClient.post()
                .uri("/chat/completions")
                .body(request)
                .exchange((clientRequest, clientResponse) -> {
                    String responseBody = readBody(clientResponse.getBody());
                    HttpStatusCode statusCode = clientResponse.getStatusCode();

                    if (!statusCode.is2xxSuccessful()) {
                        throw new IllegalStateException("OpenAI API 호출 실패: " + responseBody);
                    }

                    OpenAiChatResponse response = objectMapper.readValue(responseBody, OpenAiChatResponse.class);
                    return response.firstContent();
                });
    }
}