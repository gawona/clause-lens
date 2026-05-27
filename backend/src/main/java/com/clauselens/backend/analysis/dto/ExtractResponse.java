package com.clauselens.backend.analysis.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

public record ExtractResponse(

        @JsonProperty("document_id")
        UUID documentId,

        @JsonProperty("page_count")
        Integer pageCount,

        List<ExtractPageResponse> pages
) {
}