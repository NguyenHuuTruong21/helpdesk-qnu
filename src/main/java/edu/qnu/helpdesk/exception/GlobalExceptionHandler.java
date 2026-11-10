package edu.qnu.helpdesk.exception;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(ResourceNotFoundException ex, Model model) {
        log.warn("Resource not found: {}", ex.getMessage());
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/404";
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNoResourceFound(NoResourceFoundException ex, Model model) {
        log.warn("Static resource or route not found: {}", ex.getResourcePath());
        model.addAttribute("errorMessage", "Không tìm thấy trang hoặc tài nguyên yêu cầu.");
        return "error/404";
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBadRequest(BadRequestException ex, Model model) {
        log.warn("Bad request: {}", ex.getMessage());
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/400";
    }

    @ExceptionHandler(FileStorageException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleFileStorage(FileStorageException ex, Model model) {
        log.error("File storage error: {}", ex.getMessage(), ex);
        model.addAttribute("errorMessage", "Lỗi xử lý file: " + ex.getMessage());
        return "error/500";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGlobal(Exception ex, Model model) {
        log.error("Unexpected error: ", ex);
        model.addAttribute("errorMessage", "Đã xảy ra lỗi không mong muốn. Vui lòng thử lại sau.");
        return "error/500";
    }
}
