package com.clauselens.backend.risk.repository;

import com.clauselens.backend.document.domain.Document;
import com.clauselens.backend.risk.domain.DocumentRiskResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DocumentRiskResultRepository extends JpaRepository<DocumentRiskResult, Long> {

    Optional<DocumentRiskResult> findByDocument(Document document);
}