package com.clauselens.backend.document.chunk.service;

import com.clauselens.backend.document.chunk.domain.ChunkType;
import com.clauselens.backend.document.chunk.domain.DocumentChunk;
import com.clauselens.backend.document.chunk.repository.DocumentChunkRepository;
import com.clauselens.backend.document.domain.Document;
import com.clauselens.backend.document.domain.DocumentPage;
import com.clauselens.backend.document.repository.DocumentPageRepository;
import com.clauselens.backend.document.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class DocumentChunkingService {

    private static final int MAX_CHUNK_LENGTH = 1200;

    private static final Pattern CLAUSE_PATTERN = Pattern.compile(
            "^(제\\s*\\d+\\s*조|제\\s*\\d+\\s*장|\\d+\\.\\s+|[가-하]\\.\\s+).*"
    );

    private final DocumentRepository documentRepository;
    private final DocumentPageRepository documentPageRepository;
    private final DocumentChunkRepository documentChunkRepository;

    @Transactional
    public void chunkDocument(UUID documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("문서를 찾을 수 없습니다. documentId=" + documentId));

        List<DocumentPage> pages = documentPageRepository.findByDocumentOrderByPageNumberAsc(document);

        if (pages.isEmpty()) {
            throw new IllegalStateException("chunking할 페이지 텍스트가 없습니다. documentId=" + documentId);
        }

        documentChunkRepository.deleteByDocument(document);

        List<DocumentChunk> chunks = new ArrayList<>();
        int chunkOrder = 1;

        for (DocumentPage page : pages) {
            List<String> chunkTexts = splitPageText(page.getContent());

            for (String chunkText : chunkTexts) {
                if (chunkText.isBlank()) {
                    continue;
                }

                DocumentChunk chunk = DocumentChunk.builder()
                        .document(document)
                        .pageNumber(page.getPageNumber())
                        .chunkOrder(chunkOrder++)
                        .sectionTitle(extractSectionTitle(chunkText))
                        .chunkType(determineChunkType(chunkText))
                        .content(chunkText.trim())
                        .build();

                chunks.add(chunk);
            }
        }

        documentChunkRepository.saveAll(chunks);
    }

    private List<String> splitPageText(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        List<String> paragraphs = splitByParagraph(text);
        List<String> result = new ArrayList<>();

        for (String paragraph : paragraphs) {
            if (paragraph.length() <= MAX_CHUNK_LENGTH) {
                result.add(paragraph);
            } else {
                result.addAll(splitByLength(paragraph));
            }
        }

        return result;
    }

    private List<String> splitByParagraph(String text) {
        return Arrays.stream(text.split("\\n\\s*\\n"))
                .map(String::trim)
                .filter(paragraph -> !paragraph.isBlank())
                .toList();
    }

    private List<String> splitByLength(String text) {
        List<String> result = new ArrayList<>();

        int start = 0;

        while (start < text.length()) {
            int end = Math.min(start + MAX_CHUNK_LENGTH, text.length());
            result.add(text.substring(start, end).trim());
            start = end;
        }

        return result;
    }

    private String extractSectionTitle(String chunkText) {
        String firstLine = chunkText.lines()
                .findFirst()
                .orElse("")
                .trim();

        if (CLAUSE_PATTERN.matcher(firstLine).matches()) {
            return firstLine;
        }

        return null;
    }

    private ChunkType determineChunkType(String chunkText) {
        String firstLine = chunkText.lines()
                .findFirst()
                .orElse("")
                .trim();

        if (CLAUSE_PATTERN.matcher(firstLine).matches()) {
            return ChunkType.CLAUSE;
        }

        return ChunkType.PARAGRAPH;
    }
}