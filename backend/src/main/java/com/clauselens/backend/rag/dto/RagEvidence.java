package com.clauselens.backend.rag.dto;

public record RagEvidence(
        Long chunkId,
        Integer pageNumber,
        String sectionTitle,
        String text,
        Double score
) {
}