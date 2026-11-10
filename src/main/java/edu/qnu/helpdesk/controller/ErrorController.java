package edu.qnu.helpdesk.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

//tùy chọn, cho trang lỗi đẹp
@Controller
public class ErrorController implements org.springframework.boot.webmvc.error.ErrorController {
    @GetMapping("/error/403")
    public String accessDenied() {
        return "error/403";
    }

    @GetMapping("/error")
    public String handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            if (statusCode == 404) {
                return "error/404";
            } else if (statusCode == 403) {
                return "error/403";
            }
        }
        return "error/500";
    }
}
