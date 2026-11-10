package edu.qnu.helpdesk.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import edu.qnu.helpdesk.entity.User;
import edu.qnu.helpdesk.enums.FeedbackCategory;
import edu.qnu.helpdesk.repository.UserRepository;
import edu.qnu.helpdesk.service.FeedbackService;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/public")
@RequiredArgsConstructor
public class PublicController {
    private final UserRepository userRepository;
    private final FeedbackService feedbackService;

    /* Trang chủ công khai | Nếu đăng nhập, redirect về trang tương ứng */
    @GetMapping({ "", "/home" })
    public String home(Authentication authentication, Model model) {

        // Nếu đã đăng nhập, redirect theo role
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username).orElse(null);
            if (user != null) {
                return user.getRole().name().equals("ADMIN")
                        ? "redirect:/admin/dashboard"
                        : "redirect:/user/home";
            }
        }

        // Public homepage
        model.addAttribute("categories", FeedbackCategory.values());
        model.addAttribute("totalTickets", feedbackService.countAll());

        return "public/home";
    }

    /* Trang Knowledge Base public */
    @GetMapping("/knowledge-base")
    public String knowledgeBase(Model model) {
        return "public/knowledge-base";
    }
}
