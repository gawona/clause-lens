package com.clauselens.backend.document.domain;

public enum AnalysisStatus {
    NOT_STARTED,
    REQUESTED,
    EXTRACTING,
    CHUNKING,
    INDEXING,
    COMPLETED,
    FAILED
}
