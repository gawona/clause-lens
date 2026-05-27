package com.clauselens.backend.search.dto;

import java.util.List;

public record DocumentSearchResponse(
        String query,
        List<DocumentSearchResult> results
) {
}