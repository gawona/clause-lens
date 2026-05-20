package com.clauselens.backend.document.repository;

import com.clauselens.backend.document.domain.Document;
import com.clauselens.backend.document.domain.DocumentPage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentPageRepository extends JpaRepository<DocumentPage, Long> {

    List<DocumentPage> findByDocumentOrderByPageNumberAsc(Document document);

    Optional<DocumentPage> findByDocumentAndPageNumber(Document document, Integer pageNumber);

    void deleteByDocument(Document document);
}