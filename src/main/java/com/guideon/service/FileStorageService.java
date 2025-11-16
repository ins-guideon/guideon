package com.guideon.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * 파일 저장 서비스
 * - 업로드 저장소(upload)에서 파일을 관리
 */
@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    private final String uploadDir;

    public FileStorageService() {
        this.uploadDir = System.getProperty("user.home") + "/guideon/uploads";

        try {
            Files.createDirectories(Paths.get(uploadDir));
            logger.info("Storage directory initialized. upload={}", uploadDir);
        } catch (Exception e) {
            logger.error("Failed to initialize storage directory", e);
        }
    }

    /**
     * 업로드 디렉토리에서 실제 파일 삭제
     */
    public void deleteFromUpload(String savedFileName) {
        try {
            if (savedFileName == null || savedFileName.isEmpty()) {
                logger.warn("deleteFromUpload called with empty file name");
                return;
            }
            Path filePath = Paths.get(uploadDir, savedFileName);
            Files.deleteIfExists(filePath);
            logger.info("Deleted uploaded file if existed: {}", filePath);
        } catch (Exception e) {
            logger.error("Failed to delete uploaded file: {}", savedFileName, e);
            throw new RuntimeException("업로드 파일 삭제 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 업로드 디렉토리에 저장
     * 파일명은 documentId를 사용합니다.
     */
    public UploadResult saveToUpload(MultipartFile file, String regulationType, String documentId) {
        try {
            validateFile(file);
            String originalFileName = file.getOriginalFilename();
            String extension = getFileExtension(originalFileName);
            String savedFileName = documentId + (extension.isEmpty() ? "" : "." + extension);

            Path targetPath = Paths.get(uploadDir, savedFileName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            logger.info("Saved file directly to upload: {} (documentId={})", savedFileName, documentId);
            return new UploadResult(documentId,
                    originalFileName != null ? originalFileName : savedFileName,
                    regulationType,
                    file.getSize(),
                    savedFileName,
                    targetPath);
        } catch (Exception e) {
            logger.error("Failed to save file to upload directory", e);
            throw new RuntimeException("업로드 디렉토리 저장 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 업로드 파일 유효성 검증
     */
    public void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드된 파일이 비어있습니다.");
        }
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new IllegalArgumentException("파일 이름이 없습니다.");
        }
        String extension = getFileExtension(fileName).toLowerCase();
        if (!isSupportedFileType(extension)) {
            throw new IllegalArgumentException("지원하지 않는 파일 형식입니다. (지원 형식: PDF, DOC, DOCX, TXT)");
        }
        long maxSize = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("파일 크기는 10MB를 초과할 수 없습니다.");
        }
    }

    private boolean isSupportedFileType(String extension) {
        return extension.equals("pdf") ||
                extension.equals("doc") ||
                extension.equals("docx") ||
                extension.equals("txt");
    }

    private String getFileExtension(String fileName) {
        if (fileName == null)
            return "";
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1);
        }
        return "";
    }

    public static class UploadResult {
        private final String id;
        private final String originalFileName;
        private final String regulationType;
        private final long fileSize;
        private final String savedFileName;
        private final Path path;

        public UploadResult(String id, String originalFileName, String regulationType, long fileSize,
                String savedFileName, Path path) {
            this.id = id;
            this.originalFileName = originalFileName;
            this.regulationType = regulationType;
            this.fileSize = fileSize;
            this.savedFileName = savedFileName;
            this.path = path;
        }

        public String getId() {
            return id;
        }

        public String getOriginalFileName() {
            return originalFileName;
        }

        public String getRegulationType() {
            return regulationType;
        }

        public long getFileSize() {
            return fileSize;
        }

        public String getSavedFileName() {
            return savedFileName;
        }

        public Path getPath() {
            return path;
        }
    }
}
