package edu.qnu.helpdesk.controller;

import java.net.MalformedURLException;
import java.nio.file.Path;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import edu.qnu.helpdesk.entity.Feedback;
import edu.qnu.helpdesk.service.FeedbackService;
import edu.qnu.helpdesk.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {
    private final FeedbackService feedbackService;
    private final FileStorageService fileStorageService;

    // Tải file đính kèm của feedback + URL: /files/download/{feedbackId}
    @GetMapping("/download/{feedbackId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long feedbackId) {
        Feedback feedback = feedbackService.getFeedbackById(feedbackId);

        if (feedback.getAttachment() == null || feedback.getAttachment().isBlank()) {
            return ResponseEntity.notFound().build();
        }

        try {
            Path filePath = fileStorageService.loadFile(feedback.getAttachment());
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                log.warn("File không tồn tại hoặc không đọc được: {}", feedback.getAttachment());
                return ResponseEntity.notFound().build();
            }

            // Tên hiển thị khi tải
            String downloadName = feedback.getAttachmentName() != null
                    ? feedback.getAttachmentName()
                    : feedback.getAttachment();

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + downloadName + "\"")
                    .body(resource);

        } catch (MalformedURLException e) {
            log.error("Lỗi đọc file: ", e);
            return ResponseEntity.badRequest().build();
        }
    }

    // Xem trực tiếp file ảnh (inline) - dùng cho preview + URL:
    // /files/view/{feedbackId}
    @GetMapping("/view/{feedbackId}")
    public ResponseEntity<Resource> viewFile(@PathVariable Long feedbackId) {
        Feedback feedback = feedbackService.getFeedbackById(feedbackId);

        if (feedback.getAttachment() == null || feedback.getAttachment().isBlank()) {
            return ResponseEntity.notFound().build();
        }

        try {
            Path filePath = fileStorageService.loadFile(feedback.getAttachment());
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            // Xác định content type theo đuôi file
            MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
            String fileName = feedback.getAttachment().toLowerCase();
            if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                mediaType = MediaType.IMAGE_JPEG;
            } else if (fileName.endsWith(".png")) {
                mediaType = MediaType.IMAGE_PNG;
            } else if (fileName.endsWith(".gif")) {
                mediaType = MediaType.IMAGE_GIF;
            } else if (fileName.endsWith(".pdf")) {
                mediaType = MediaType.APPLICATION_PDF;
            }

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                    .body(resource);

        } catch (MalformedURLException e) {
            log.error("Lỗi đọc file: ", e);
            return ResponseEntity.badRequest().build();
        }
    }
}
