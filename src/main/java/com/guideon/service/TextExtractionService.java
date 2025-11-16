package com.guideon.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.parser.apache.poi.ApachePoiDocumentParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.InputStream;
import org.springframework.web.multipart.MultipartFile;

/**
 * 텍스트 추출 서비스
 * 파일 경로를 받아 타입에 따라 본문 텍스트를 추출
 */
@Service
public class TextExtractionService {

    private static final Logger logger = LoggerFactory.getLogger(TextExtractionService.class);

    private final DocumentParser pdfParser = new ApachePdfBoxDocumentParser();
    private final DocumentParser docParser = new ApachePoiDocumentParser();

    public String extract(MultipartFile file) {
        try {
            String fileName = file.getOriginalFilename();
            if (fileName == null) {
                throw new IllegalArgumentException("파일 이름이 없습니다.");
            }
            String extension = getFileExtension(fileName).toLowerCase();

            try (InputStream inputStream = file.getInputStream()) {
                Document document;
                switch (extension) {
                    case "pdf":
                        document = pdfParser.parse(inputStream);
                        break;
                    case "doc":
                    case "docx":
                        document = docParser.parse(inputStream);
                        break;
                    case "txt":
                        return new String(file.getBytes(), java.nio.charset.StandardCharsets.UTF_8);
                    default:
                        throw new IllegalArgumentException("지원하지 않는 파일 형식: " + extension);
                }
                logger.debug("Extracted text: {} chars from {}", document.text().length(), fileName);
                return document.text();
            }
        } catch (Exception e) {
            logger.error("Failed to extract text", e);
            throw new RuntimeException("텍스트 추출 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    public String extract(Path filePath) {
        try {
            String fileName = filePath.getFileName().toString();
            String extension = getFileExtension(fileName).toLowerCase();

            try (InputStream inputStream = Files.newInputStream(filePath)) {
                Document document;
                switch (extension) {
                    case "pdf":
                        document = pdfParser.parse(inputStream);
                        break;
                    case "doc":
                    case "docx":
                        document = docParser.parse(inputStream);
                        break;
                    case "txt":
                        return Files.readString(filePath);
                    default:
                        throw new IllegalArgumentException("지원하지 않는 파일 형식: " + extension);
                }
                logger.debug("Extracted text: {} chars from {}", document.text().length(), fileName);
                return document.text();
            }
        } catch (Exception e) {
            logger.error("Failed to extract text", e);
            throw new RuntimeException("텍스트 추출 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1);
        }
        return "";
    }
}
