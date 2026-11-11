package edu.qnu.helpdesk.controller;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
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

import edu.qnu.helpdesk.dto.ReplyRequest;
import edu.qnu.helpdesk.entity.Feedback;
import edu.qnu.helpdesk.entity.User;
import edu.qnu.helpdesk.enums.FeedbackStatus;
import edu.qnu.helpdesk.repository.UserRepository.UserSummary;
import edu.qnu.helpdesk.service.FeedbackService;
import edu.qnu.helpdesk.service.ReplyService;
import edu.qnu.helpdesk.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final UserService userService;
    private final FeedbackService feedbackService;
    private final ReplyService replyService;

    // Dashboard - Thống kê tổng quan
    @GetMapping({ "", "/", "/dashboard" })
    public String dashboard(Model model) {
        long total = feedbackService.countAll();
        long pending = feedbackService.countByStatus(FeedbackStatus.PENDING);
        long processing = feedbackService.countByStatus(FeedbackStatus.PROCESSING);
        long resolved = feedbackService.countByStatus(FeedbackStatus.RESOLVED);

        long userCount = userService.countUsers();
        long adminCount = userService.countAdmins();

        // 5 phản hồi mới nhất (chờ xử lý)
        Pageable top5 = PageRequest.of(0, 5);
        Page<Feedback> recentPending = feedbackService.filterFeedbacks(
                FeedbackStatus.PENDING, "", top5);

        model.addAttribute("total", total);
        model.addAttribute("pending", pending);
        model.addAttribute("processing", processing);
        model.addAttribute("resolved", resolved);
        model.addAttribute("userCount", userCount);
        model.addAttribute("adminCount", adminCount);
        model.addAttribute("recentPending", recentPending.getContent());

        return "admin/dashboard";
    }

    // Danh sách tất cả phản hồi (có filter + phân trang)
    @GetMapping("/feedbacks")
    public String listFeedbacks(@RequestParam(required = false) FeedbackStatus status,
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Feedback> feedbackPage = feedbackService.filterFeedbacks(status, keyword, pageable);

        model.addAttribute("feedbackPage", feedbackPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", feedbackPage.getTotalPages());
        model.addAttribute("status", status);
        model.addAttribute("keyword", keyword);
        model.addAttribute("statuses", FeedbackStatus.values());

        return "admin/feedback-list";
    }

    // Danh sách người dùng (phân trang)
    @GetMapping("/users")
    public String listUsers(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserSummary> userPage = userService.findUsers(pageable);

        model.addAttribute("userPage", userPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);

        return "admin/user-list";
    }

    // Xem chi tiết phản hồi + form trả lời
    @GetMapping("/feedbacks/{id}")
    public String viewFeedbackDetail(@PathVariable Long id, Model model) {
        Feedback feedback = feedbackService.getFeedbackById(id);

        if (!model.containsAttribute("replyRequest")) {
            ReplyRequest replyRequest = new ReplyRequest();
            // Mặc định trạng thái = Đang xử lý nếu feedback đang PENDING
            replyRequest.setStatus(feedback.getStatus() == FeedbackStatus.PENDING
                    ? FeedbackStatus.PROCESSING
                    : feedback.getStatus());
            model.addAttribute("replyRequest", replyRequest);
        }

        model.addAttribute("feedback", feedback);
        model.addAttribute("replies", replyService.getRepliesByFeedback(feedback));
        model.addAttribute("statuses", FeedbackStatus.values());

        return "admin/feedback-detail";
    }

    // Xử lý admin trả lời phản hồi
    @PostMapping("/feedbacks/{id}/reply")
    public String replyToFeedback(@PathVariable Long id,
            @Valid @ModelAttribute("replyRequest") ReplyRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        Feedback feedback = feedbackService.getFeedbackById(id);

        if (bindingResult.hasErrors()) {
            model.addAttribute("feedback", feedback);
            model.addAttribute("replies", replyService.getRepliesByFeedback(feedback));
            model.addAttribute("statuses", FeedbackStatus.values());
            model.addAttribute("errorMessage", "Vui lòng kiểm tra lại nội dung trả lời");
            return "admin/feedback-detail";
        }

        try {
            User admin = userService.getCurrentUser();
            replyService.createReply(id, request, admin);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã gửi phản hồi cho người dùng thành công!");
            return "redirect:/admin/feedbacks/" + id;
        } catch (Exception e) {
            log.error("Lỗi trả lời phản hồi: ", e);
            model.addAttribute("feedback", feedback);
            model.addAttribute("replies", replyService.getRepliesByFeedback(feedback));
            model.addAttribute("statuses", FeedbackStatus.values());
            model.addAttribute("errorMessage", "Không thể gửi trả lời: " + e.getMessage());
            return "admin/feedback-detail";
        }
    }

    // Cập nhật trạng thái nhanh (AJAX / form nhỏ)
    @PostMapping("/feedbacks/{id}/status")
    public String updateStatus(@PathVariable Long id,
            @RequestParam FeedbackStatus status,
            RedirectAttributes redirectAttributes) {
        feedbackService.updateStatus(id, status);
        redirectAttributes.addFlashAttribute("successMessage",
                "Đã cập nhật trạng thái thành: " + status.getLabel());
        return "redirect:/admin/feedbacks/" + id;
    }

    @PostMapping("/feedbacks/{id}/approve-citizen-id")
    public String approveCitizenIdUpdate(@PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        feedbackService.approveCitizenIdUpdate(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã duyệt và đồng bộ CCCD vào hồ sơ tài khoản.");
        return "redirect:/admin/feedbacks/" + id;
    }

    // Xoá phản hồi
    @PostMapping("/feedbacks/{id}/delete")
    public String deleteFeedback(@PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        try {
            feedbackService.deleteFeedback(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã xóa phản hồi #" + id);
        } catch (Exception e) {
            log.error("Lỗi xóa phản hồi: ", e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Không thể xóa phản hồi: " + e.getMessage());
        }
        return "redirect:/admin/feedbacks";
    }

    @PostMapping("/feedbacks/{id}/appointment")
    public String setAppointment(@PathVariable Long id,
                                 @RequestParam String room,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime time,
                                 @RequestParam String staff,
                                 RedirectAttributes redirectAttributes) {
        Feedback fb = feedbackService.getFeedbackById(id);
        fb.setAppointmentRoom(room);
        fb.setAppointmentTime(time);
        fb.setAppointmentStaff(staff);
        fb.setStatus(FeedbackStatus.PROCESSING);
        feedbackService.updateStatus(id, FeedbackStatus.PROCESSING); // Hàm có sẵn
        // Lưu ý: Cần thêm hàm save trong FeedbackService
        feedbackService.save(fb); 
        
        redirectAttributes.addFlashAttribute("successMessage", "Đã tạo lịch hẹn thành công!");
        return "redirect:/admin/feedbacks/" + id;
    }

    @PostMapping("/feedbacks/{id}/chat")
    public String adminChat(@PathVariable Long id, @RequestParam String message) {
        User admin = userService.getCurrentUser();
        ReplyRequest req = new ReplyRequest();
        req.setContent(message);
        req.setStatus(FeedbackStatus.PROCESSING); // Đang xử lý
        replyService.createReply(id, req, admin);
        return "redirect:/admin/feedbacks/" + id;
    }
}
