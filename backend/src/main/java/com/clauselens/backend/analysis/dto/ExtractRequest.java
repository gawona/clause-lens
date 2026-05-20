package com.clauselens.backend.analysis.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record ExtractRequest(

        @JsonProperty("document_id")
        UUID documentId,

        @JsonProperty("file_path")
        String filePath
) {
}