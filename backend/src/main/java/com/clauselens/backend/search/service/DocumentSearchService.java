package com.clauselens.backend.search.service;

import com.clauselens.backend.search.config.OpenSearchProperties;
import com.clauselens.backend.search.dto.DocumentSearchResponse;
import com.clauselens.backend.search.dto.DocumentSearchResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentSearchService {

    private final RestClient openSearchRestClient;
    private final OpenSearchProperties openSearchProperties;
    private final ObjectMapper objectMapper;

    public DocumentSearchResponse search(UUID documentId, String query, int size) {
        String indexName = openSearchProperties.getIndex().getDocumentChunks();

        String requestBody = """
            {
              "size": %d,
              "query": {
                "bool": {
                  "filter": [
                    {
                      "term": {
                        "documentId": "%s"
                      }
                    }
                  ],
                  "must": [
                    {
                      "multi_match": {
                        "query": "%s",
                        "fields": [
                          "content^3",
                          "sectionTitle^2",
                          "chunkType"
                        ]
                      }
                    }
                  ]
                }
              }
            }
            """.formatted(size, documentId, escapeJson(query));

        String responseBody = openSearchRestClient.post()
                .uri("/{indexName}/_search", indexName)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(String.class);

        JsonNode response = parseJson(responseBody);

        return new DocumentSearchResponse(query, parseResults(response));
    }

    public DocumentSearchResponse search(UUID documentId, String query) {
        return search(documentId, query, 10);
    }

    private List<DocumentSearchResult> parseResults(JsonNode response) {
        List<DocumentSearchResult> results = new ArrayList<>();

        if (response == null || response.path("hits").path("hits").isMissingNode()) {
            return results;
        }

        for (JsonNode hit : response.path("hits").path("hits")) {
            JsonNode source = hit.path("_source");

            DocumentSearchResult result = new DocumentSearchResult(
                    source.path("chunkId").asLong(),
                    source.path("documentId").asText(),
                    source.path("pageNumber").asInt(),
                    source.path("chunkOrder").asInt(),
                    source.path("sectionTitle").isNull() ? null : source.path("sectionTitle").asText(),
                    source.path("chunkType").asText(),
                    source.path("content").asText(),
                    hit.path("_score").asDouble()
            );

            results.add(result);
        }

        return results;
    }

    private String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    private JsonNode parseJson(String responseBody) {
        try {
            return objectMapper.readTree(responseBody);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("OpenSearch 검색 응답 JSON 파싱 중 오류가 발생했습니다.", e);
        }
    }
}