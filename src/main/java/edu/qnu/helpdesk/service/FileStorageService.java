package edu.qnu.helpdesk.service;

import java.nio.file.Path;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    /*
     * Lưu file và trả về tên file đã lưu (unique)
     */
    String storeFile(MultipartFile file);

    /*
     * Xóa file theo tên đã lưu
     */
    void deleteFile(String storedFileName);

    /*
     * Load đường dẫn tuyệt đối của file
     */
    Path loadFile(String storedFileName);

    /*
     * Kiểm tra file có phải ảnh không
     */
    boolean isImage(String fileName);
}
