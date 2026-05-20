package com.clauselens.backend.document.dto;

import com.clauselens.backend.document.domain.DocumentPage;

public record DocumentPageResponse(
        Long id,
        Integer page,
        String text
) {

    public static DocumentPageResponse from(DocumentPage documentPage) {
        return new DocumentPageResponse(
                documentPage.getId(),
                documentPage.getPageNumber(),
                documentPage.getContent()
        );
    }
}