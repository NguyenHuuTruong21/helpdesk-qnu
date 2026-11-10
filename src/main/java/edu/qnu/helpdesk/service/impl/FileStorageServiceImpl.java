package edu.qnu.helpdesk.service.impl;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import edu.qnu.helpdesk.exception.FileStorageException;
import edu.qnu.helpdesk.service.FileStorageService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FileStorageServiceImpl implements FileStorageService {
    @Value("${app.upload.dir:uploads/}")
    private String uploadDir;

    private Path rootLocation;
    // Định dạng cho phép
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "pdf", "doc", "docx", "xls", "xlsx", "txt", "zip");

    private static final List<String> IMAGE_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "gif");

    @PostConstruct
    public void init() {
        try {
            this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(this.rootLocation);
            log.info("Upload directory: {}", this.rootLocation);
        } catch (IOException e) {
            throw new FileStorageException("Không thể khởi tạo thư mục upload", e);
        }
    }

    @Override
    public String storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String originalName = StringUtils.cleanPath(file.getOriginalFilename());
        if (originalName.contains("..")) {
            throw new FileStorageException("Tên file không hợp lệ: " + originalName);
        }

        String extension = getExtension(originalName);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new FileStorageException("Định dạng file không được phép: ." + extension);
        }

        // Tên file unique: UUID + timestamp + extension
        String storedFileName = UUID.randomUUID().toString()
                + "_" + System.currentTimeMillis()
                + "." + extension;

        try {
            Path targetLocation = this.rootLocation.resolve(storedFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            log.info("Stored file: {}", storedFileName);
            return storedFileName;
        } catch (IOException e) {
            throw new FileStorageException("Không thể lưu file " + originalName, e);
        }
    }

    @Override
    public void deleteFile(String storedFileName) {
        if (storedFileName == null || storedFileName.isBlank())
            return;
        try {
            Path filePath = this.rootLocation.resolve(storedFileName).normalize();
            Files.deleteIfExists(filePath);
            log.info("Deleted file: {}", storedFileName);
        } catch (IOException e) {
            log.warn("Không thể xóa file {}: {}", storedFileName, e.getMessage());
        }
    }

    @Override
    public Path loadFile(String storedFileName) {
        return this.rootLocation.resolve(storedFileName).normalize();
    }

    @Override
    public boolean isImage(String fileName) {
        if (fileName == null)
            return false;
        String ext = getExtension(fileName).toLowerCase();
        return IMAGE_EXTENSIONS.contains(ext);
    }

    private String getExtension(String fileName) {
        if (fileName == null || !fileName.contains("."))
            return "";
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
}
