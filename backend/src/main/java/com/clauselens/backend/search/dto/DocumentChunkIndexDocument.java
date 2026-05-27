package com.clauselens.backend.search.dto;

public record DocumentChunkIndexDocument(
        Long chunkId,
        String documentId,
        Integer pageNumber,
        Integer chunkOrder,
        String sectionTitle,
        String chunkType,
        String content
) {
}