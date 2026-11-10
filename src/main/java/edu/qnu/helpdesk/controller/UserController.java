package edu.qnu.helpdesk.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import edu.qnu.helpdesk.dto.FeedbackRequest;
import edu.qnu.helpdesk.entity.Feedback;
import edu.qnu.helpdesk.entity.User;
import edu.qnu.helpdesk.enums.FeedbackCategory;
import edu.qnu.helpdesk.service.FeedbackService;
import edu.qnu.helpdesk.service.ReplyService;
import edu.qnu.helpdesk.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final FeedbackService feedbackService;
    private final ReplyService replyService;

    // Danh sách hiện tại của user hiện tại (phân trang)
    @GetMapping("/feedbacks")
    public String listFeedbacks(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        User user = userService.getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);
        Page<Feedback> feedbackPage = feedbackService.getFeedbacksByUser(user, pageable);

        model.addAttribute("user", user);
        model.addAttribute("feedbackPage", feedbackPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", feedbackPage.getTotalPages());

        return "user/feedback-list";
    }

    // Hiển thị form gửi phản hồi mới
    @GetMapping("/feedbacks/new")
    public String showCreateForm(Model model) {
        if (!model.containsAttribute("feedbackRequest")) {
            model.addAttribute("feedbackRequest", new FeedbackRequest());
        }
        model.addAttribute("categories", FeedbackCategory.values());
        return "user/feedback-create";
    }

    // Xử lý gửi phản hồi mới
    @PostMapping("/feedbacks/new")
    public String createFeedback(@Valid @ModelAttribute("feedbackRequest") FeedbackRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", FeedbackCategory.values());
            return "user/feedback-create";
        }

        try {
            User user = userService.getCurrentUser();
            Feedback feedback = feedbackService.createFeedback(request, user);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Gửi phản hồi thành công! Mã phản hồi của bạn là #" + feedback.getId());
            return "redirect:/user/feedbacks";
        } catch (Exception e) {
            log.error("Lỗi gửi phản hồi: ", e);
            model.addAttribute("errorMessage", "Không thể gửi phản hồi: " + e.getMessage());
            model.addAttribute("categories", FeedbackCategory.values());
            return "user/feedback-create";
        }
    }

    // Xem chi tiết phản hồi + các reply của admin
    @GetMapping("/feedbacks/{id}")
    public String viewFeedbackDetail(@PathVariable Long id, Model model) {
        User user = userService.getCurrentUser();
        Feedback feedback = feedbackService.getFeedbackByIdAndUser(id, user);

        model.addAttribute("feedback", feedback);
        model.addAttribute("replies", replyService.getRepliesByFeedback(feedback));
        model.addAttribute("user", user);

        return "user/feedback-detail";
    }

        @PostMapping("/feedbacks/{id}/chat")
        public String userChat(@PathVariable Long id, @RequestParam String message) {
            // 1. Lấy user hiện tại
            User user = userService.getCurrentUser();
            // 2. Lấy ticket
            Feedback fb = feedbackService.getFeedbackByIdAndUser(id, user);
            // 3. Gọi hàm vừa thêm ở Bước 1
            replyService.createUserReply(fb, message); 
            // 4. Load lại trang chi tiết
        return "redirect:/user/feedbacks/" + id;
    }
}
