package com.guideon.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 파일 저장 서비스
 * - 임시 저장소(temp)와 실제 업로드 저장소(upload)로 분리하여 관리
 * - uploadId로 temp 파일을 추적하고, 확정 시 upload 디렉토리로 이동
 */
@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    private final String tempDir;
    private final String uploadDir;

    // TODO: 재시작 했을 때, uploadMap을 채워둘지 말지. 이 map의 의미가 있는지

    private final Map<String, TempUploadInfo> uploadMap = new ConcurrentHashMap<>();

    public FileStorageService() {
        this.tempDir = System.getProperty("user.home") + "/guideon/temp";
        this.uploadDir = System.getProperty("user.home") + "/guideon/uploads";

        try {
            Files.createDirectories(Paths.get(tempDir));
            Files.createDirectories(Paths.get(uploadDir));
            logger.info("Storage directories initialized. temp={}, upload={}", tempDir, uploadDir);
        } catch (Exception e) {
            logger.error("Failed to initialize storage directories", e);
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

    public TempUploadInfo saveTemp(MultipartFile file, String regulationType) {
        try {
            // 파일 유효성 검증
            validateFile(file);

            String originalFileName = file.getOriginalFilename();
            String extension = getFileExtension(originalFileName);
            String id = UUID.randomUUID().toString();
            String savedTempFileName = id + (extension.isEmpty() ? "" : "." + extension);

            Path targetPath = Paths.get(tempDir, savedTempFileName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            TempUploadInfo info = new TempUploadInfo(
                    id,
                    originalFileName != null ? originalFileName : savedTempFileName,
                    regulationType,
                    file.getSize(),
                    savedTempFileName,
                    System.currentTimeMillis());
            uploadMap.put(id, info);

            logger.info("Saved temp file: {} (id={})", savedTempFileName, id);
            return info;
        } catch (Exception e) {
            logger.error("Failed to save temp file", e);
            throw new RuntimeException("임시 파일 저장 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 처음부터 업로드 디렉토리에 저장(원샷 업로드용)
     */
    public UploadResult saveToUpload(MultipartFile file, String regulationType) {
        try {
            validateFile(file);
            String originalFileName = file.getOriginalFilename();
            String extension = getFileExtension(originalFileName);
            String id = UUID.randomUUID().toString();
            String savedFileName = id + (extension.isEmpty() ? "" : "." + extension);

            Path targetPath = Paths.get(uploadDir, savedFileName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            logger.info("Saved file directly to upload: {} (id={})", savedFileName, id);
            return new UploadResult(id,
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

    public Path getTempPath(String uuid) {
        TempUploadInfo info = uploadMap.get(uuid);
        if (info == null) {
            return null;
        }
        return Paths.get(tempDir, info.getSavedTempFileName());
    }

    public MoveResult moveToUpload(String uuid) {
        TempUploadInfo info = uploadMap.get(uuid);
        if (info == null) {
            throw new IllegalArgumentException("업로드 ID를 찾을 수 없습니다: " + uuid);
        }

        try {
            Path tempPath = Paths.get(tempDir, info.getSavedTempFileName());
            if (!Files.exists(tempPath)) {
                throw new IllegalStateException("임시 파일이 존재하지 않습니다: " + tempPath);
            }
            Path finalPath = Paths.get(uploadDir, info.getSavedTempFileName());
            Files.move(tempPath, finalPath, StandardCopyOption.REPLACE_EXISTING);
            uploadMap.remove(uuid);

            logger.info("Moved file from temp to upload: {}", finalPath);
            return new MoveResult(finalPath, info);
        } catch (Exception e) {
            logger.error("Failed to move file to upload directory", e);
            throw new RuntimeException("업로드 디렉토리 이동 중 오류가 발생했습니다: " + e.getMessage(), e);
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

    public static class TempUploadInfo {
        private final String id;
        private final String originalFileName;
        private final String regulationType;
        private final long fileSize;
        private final String savedTempFileName;
        private final long uploadTimestamp;

        public TempUploadInfo(String id, String originalFileName, String regulationType, long fileSize,
                String savedTempFileName, long uploadTimestamp) {
            this.id = id;
            this.originalFileName = originalFileName;
            this.regulationType = regulationType;
            this.fileSize = fileSize;
            this.savedTempFileName = savedTempFileName;
            this.uploadTimestamp = uploadTimestamp;
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

        public String getSavedTempFileName() {
            return savedTempFileName;
        }

        public long getUploadTimestamp() {
            return uploadTimestamp;
        }
    }

    public static class MoveResult {
        private final Path finalPath;
        private final TempUploadInfo info;

        public MoveResult(Path finalPath, TempUploadInfo info) {
            this.finalPath = finalPath;
            this.info = info;
        }

        public Path getFinalPath() {
            return finalPath;
        }

        public TempUploadInfo getInfo() {
            return info;
        }

        public String getFinalFileName() {
            return info.getSavedTempFileName();
        }
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
