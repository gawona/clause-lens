package com.clauselens.backend.extraction.repository;

import com.clauselens.backend.document.domain.Document;
import com.clauselens.backend.extraction.domain.DocumentExtractionResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DocumentExtractionResultRepository extends JpaRepository<DocumentExtractionResult, Long> {

    Optional<DocumentExtractionResult> findByDocument(Document document);
}