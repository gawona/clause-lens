package com.clauselens.backend.search.service;

import com.clauselens.backend.document.chunk.domain.DocumentChunk;
import com.clauselens.backend.document.chunk.repository.DocumentChunkRepository;
import com.clauselens.backend.document.domain.Document;
import com.clauselens.backend.document.repository.DocumentRepository;
import com.clauselens.backend.search.config.OpenSearchProperties;
import com.clauselens.backend.search.dto.DocumentChunkIndexDocument;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.nio.charset.StandardCharsets;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentIndexService {

    private static final MediaType NDJSON = MediaType.parseMediaType("application/x-ndjson;charset=UTF-8");

    private final RestClient openSearchRestClient;
    private final ObjectMapper objectMapper;
    private final OpenSearchProperties openSearchProperties;
    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository documentChunkRepository;

    public void indexDocumentChunks(UUID documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("문서를 찾을 수 없습니다. documentId=" + documentId));

        List<DocumentChunk> chunks = documentChunkRepository.findByDocumentOrderByChunkOrderAsc(document);

        if (chunks.isEmpty()) {
            throw new IllegalStateException("색인할 chunk가 없습니다. documentId=" + documentId);
        }

        createIndexIfNotExists();

        String bulkBody = buildBulkRequestBody(chunks);

        openSearchRestClient.post()
                .uri("/_bulk")
                .contentType(NDJSON)
                .body(bulkBody.getBytes(StandardCharsets.UTF_8))
                .retrieve()
                .toBodilessEntity();

        refreshDocumentChunksIndex();
    }

    private void createIndexIfNotExists() {
        String indexName = openSearchProperties.getIndex().getDocumentChunks();

        try {
            openSearchRestClient.head()
                    .uri("/{indexName}", indexName)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            createIndex(indexName);
        }
    }

    private void createIndex(String indexName) {
        String indexMapping = """
                {
                  "settings": {
                    "analysis": {
                      "analyzer": {
                        "korean_analyzer": {
                          "type": "custom",
                          "tokenizer": "standard",
                          "filter": ["lowercase"]
                        }
                      }
                    }
                  },
                  "mappings": {
                    "properties": {
                      "chunkId": { "type": "long" },
                      "documentId": { "type": "keyword" },
                      "pageNumber": { "type": "integer" },
                      "chunkOrder": { "type": "integer" },
                      "sectionTitle": {
                        "type": "text",
                        "analyzer": "korean_analyzer",
                        "fields": {
                          "keyword": { "type": "keyword" }
                        }
                      },
                      "chunkType": { "type": "keyword" },
                      "content": {
                        "type": "text",
                        "analyzer": "korean_analyzer"
                      }
                    }
                  }
                }
                """;

        openSearchRestClient.put()
                .uri("/{indexName}", indexName)
                .contentType(MediaType.APPLICATION_JSON)
                .body(indexMapping)
                .retrieve()
                .toBodilessEntity();
    }

    private String buildBulkRequestBody(List<DocumentChunk> chunks) {
        String indexName = openSearchProperties.getIndex().getDocumentChunks();
        StringBuilder builder = new StringBuilder();

        for (DocumentChunk chunk : chunks) {
            appendJsonLine(builder, new BulkIndexMetadata(
                    new BulkIndexInfo(indexName, String.valueOf(chunk.getId()))
            ));

            appendJsonLine(builder, toIndexDocument(chunk));
        }

        return builder.toString();
    }

    private DocumentChunkIndexDocument toIndexDocument(DocumentChunk chunk) {
        return new DocumentChunkIndexDocument(
                chunk.getId(),
                chunk.getDocument().getId().toString(),
                chunk.getPageNumber(),
                chunk.getChunkOrder(),
                chunk.getSectionTitle(),
                chunk.getChunkType().name(),
                chunk.getContent()
        );
    }

    private void appendJsonLine(StringBuilder builder, Object value) {
        try {
            builder.append(objectMapper.writeValueAsString(value)).append("\n");
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("OpenSearch bulk 요청 JSON 생성 중 오류가 발생했습니다.", e);
        }
    }

    private record BulkIndexMetadata(BulkIndexInfo index) {
    }

    private record BulkIndexInfo(String _index, String _id) {
    }

    private void refreshDocumentChunksIndex() {
        String indexName = openSearchProperties.getIndex().getDocumentChunks();

        openSearchRestClient.post()
                .uri("/{indexName}/_refresh", indexName)
                .retrieve()
                .toBodilessEntity();
    }
}