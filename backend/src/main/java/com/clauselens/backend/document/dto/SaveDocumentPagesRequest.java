package com.clauselens.backend.document.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record SaveDocumentPagesRequest(

        @Valid
        @NotEmpty(message = "저장할 페이지 목록이 필요합니다.")
        List<SaveDocumentPageRequest> pages
) {
}