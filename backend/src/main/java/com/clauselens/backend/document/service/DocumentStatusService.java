package com.clauselens.backend.document.service;

import com.clauselens.backend.document.domain.Document;
import com.clauselens.backend.document.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class DocumentStatusService {

    private final DocumentRepository documentRepository;

    @Transactional
    public void updateStatus(UUID documentId, Consumer<Document> statusUpdater) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("문서를 찾을 수 없습니다. documentId=" + documentId));

        statusUpdater.accept(document);
    }
}