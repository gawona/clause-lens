package com.clauselens.backend.extraction.dto;

public record ExtractionEvidence(
        Long chunkId,
        Integer pageNumber,
        String sectionTitle,
        String text,
        Double score
) {
}