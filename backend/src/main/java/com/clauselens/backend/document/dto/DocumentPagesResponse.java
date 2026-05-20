package com.clauselens.backend.document.dto;

import java.util.List;
import java.util.UUID;

public record DocumentPagesResponse(
        UUID documentId,
        int pageCount,
        List<DocumentPageResponse> pages
) {
}