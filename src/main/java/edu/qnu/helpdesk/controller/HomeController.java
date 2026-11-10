package edu.qnu.helpdesk.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import edu.qnu.helpdesk.entity.User;
import edu.qnu.helpdesk.enums.FeedbackStatus;
import edu.qnu.helpdesk.enums.Role;
import edu.qnu.helpdesk.service.FeedbackService;
import edu.qnu.helpdesk.service.UserService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private final UserService userService;
    private final FeedbackService feedbackService;

    // Trang chủ gốc của ứng dụng
    @GetMapping({ "/", "/dashboard" })
    public String home(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/redirect";
        }
        return "redirect:/public/home";
    }

    // Redirect /knowledge-base → /public/knowledge-base
    // @GetMapping("/knowledge-base")
    // public String knowledgeBase() {
    // return "redirect:/public/knowledge-base";
    // }

    // Điều hướng theo role sau khi đăng nhập thành công
    @GetMapping("/redirect")
    public String redirectByRole(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        User user = userService.getCurrentUser();

        if (user.getRole() == Role.ADMIN) {
            return "redirect:/admin/dashboard";
        }
        return "redirect:/user/home";
    }

    // Trang chủ của user (hiển thị thống kê cá nhân)
    @GetMapping("/user/home")
    public String userHome(Model model) {
        User user = userService.getCurrentUser();

        long total = feedbackService.countByUser(user);
        long pending = feedbackService.countByUserAndStatus(user, FeedbackStatus.PENDING);
        long processing = feedbackService.countByUserAndStatus(user, FeedbackStatus.PROCESSING);
        long resolved = feedbackService.countByUserAndStatus(user, FeedbackStatus.RESOLVED);

        model.addAttribute("user", user);
        model.addAttribute("total", total);
        model.addAttribute("pending", pending);
        model.addAttribute("processing", processing);
        model.addAttribute("resolved", resolved);

        return "user/home";
    }
}
