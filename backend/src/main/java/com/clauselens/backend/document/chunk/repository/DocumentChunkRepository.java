package com.clauselens.backend.document.chunk.repository;

import com.clauselens.backend.document.chunk.domain.DocumentChunk;
import com.clauselens.backend.document.domain.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {

    List<DocumentChunk> findByDocumentOrderByChunkOrderAsc(Document document);

    void deleteByDocument(Document document);

    List<DocumentChunk> findTop5ByDocumentOrderByPageNumberAscChunkOrderAsc(Document document);
}