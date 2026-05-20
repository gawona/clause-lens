package com.clauselens.backend.document.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path uploadPath;

    public FileStorageService(
            @Value("${file.upload-dir}") String uploadDir
    ) {
        this.uploadPath = Path.of(uploadDir)
                .toAbsolutePath()
                .normalize();
    }

    public StoredFile storePdf(MultipartFile file) {
        validatePdf(file);

        try {
            Files.createDirectories(uploadPath);

            String storedFilename = UUID.randomUUID() + ".pdf";
            Path targetPath = uploadPath.resolve(storedFilename)
                    .normalize();

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            return new StoredFile(
                    storedFilename,
                    targetPath.toString()
            );
        } catch (IOException e) {
            throw new IllegalStateException("PDF 파일 저장에 실패했습니다. 원인: " + e.getMessage(), e);
        }
    }

    private void validatePdf(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 PDF 파일이 없습니다.");
        }

        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("파일명이 올바르지 않습니다.");
        }

        if (!originalFilename.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("PDF 파일만 업로드할 수 있습니다.");
        }
    }

    public record StoredFile(
            String storedFilename,
            String filePath
    ) {
    }
}