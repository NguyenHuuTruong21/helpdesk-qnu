package edu.qnu.helpdesk.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import edu.qnu.helpdesk.dto.GuestTicketRequest;
import edu.qnu.helpdesk.dto.TrackTicketRequest;
import edu.qnu.helpdesk.entity.Feedback;
import edu.qnu.helpdesk.enums.FeedbackCategory;
import edu.qnu.helpdesk.exception.BadRequestException;
import edu.qnu.helpdesk.exception.ResourceNotFoundException;
import edu.qnu.helpdesk.service.GuestTicketService;
import edu.qnu.helpdesk.service.ReplyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class GuestTicketController {
    private final GuestTicketService guestTicketService;
    private final ReplyService replyService;

    /* Bước 1: Chọn danh mục */
    @GetMapping("/submit-ticket")
    public String selectCategory(Model model) {
        model.addAttribute("categories", FeedbackCategory.values());
        return "public/category-select";
    }

    /* Bước 2: Form submit theo danh mục đã chọn */
    @GetMapping("/submit-ticket/{category}")
    public String showSubmitForm(@PathVariable String category, Model model) {
        FeedbackCategory cat;
        try {
            cat = FeedbackCategory.valueOf(category);
        } catch (IllegalArgumentException e) {
            return "redirect:/submit-ticket";
        }

        if (!model.containsAttribute("request")) {
            GuestTicketRequest request = new GuestTicketRequest();
            request.setCategory(cat);
            model.addAttribute("request", request);
        }
        model.addAttribute("category", cat);
        model.addAttribute("categories", FeedbackCategory.values());

        return "public/submit-ticket";
    }

    /* Xử lý submit ticket */
    @PostMapping("/submit-ticket")
    public String submitTicket(@Valid @ModelAttribute("request") GuestTicketRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("category", request.getCategory());
            model.addAttribute("categories", FeedbackCategory.values());
            return "public/submit-ticket";
        }

        try {
            Feedback feedback = guestTicketService.createGuestTicket(request);

            // Chuyển sang trang thành công với mã tracking
            redirectAttributes.addFlashAttribute("trackingCode", feedback.getTrackingCode());
            redirectAttributes.addFlashAttribute("guestEmail", request.getEmail());
            redirectAttributes.addFlashAttribute("feedbackId", feedback.getId());

            return "redirect:/submit-ticket/success";
        } catch (Exception e) {
            log.error("Lỗi tạo ticket guest: ", e);
            model.addAttribute("errorMessage", "Không thể gửi yêu cầu: " + e.getMessage());
            model.addAttribute("category", request.getCategory());
            model.addAttribute("categories", FeedbackCategory.values());
            return "public/submit-ticket";
        }
    }

    @GetMapping("/submit-ticket/success")
    public String submitSuccess(Model model) {
        if (!model.containsAttribute("trackingCode")) {
            return "redirect:/submit-ticket";
        }
        return "public/submit-success";
    }

    // View / Track ticket
    /* Form tra cứu ticket */
    @GetMapping("/view-ticket")
    public String showTrackForm(Model model) {
        if (!model.containsAttribute("request")) {
            model.addAttribute("request", new TrackTicketRequest());
        }
        return "public/view-ticket";
    }

    /* Xử lý tra cứu */
    @PostMapping("/view-ticket")
    public String trackTicket(@Valid @ModelAttribute("request") TrackTicketRequest request,
            BindingResult bindingResult,
            Model model) {
        if (bindingResult.hasErrors()) {
            return "public/view-ticket";
        }

        try {
            Feedback feedback = guestTicketService.findByTrackingCodeAndEmail(
                    request.getTrackingCode(), request.getEmail());

            return "redirect:/view-ticket/" + feedback.getTrackingCode()
                    + "?email=" + request.getEmail();
        } catch (ResourceNotFoundException | BadRequestException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "public/view-ticket";
        }
    }

    /* Xem chi tiết ticket */
    @GetMapping("/view-ticket/{trackingCode}")
    public String viewTicketDetail(@PathVariable String trackingCode,
            @RequestParam("email") String email,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            Feedback feedback = guestTicketService.findByTrackingCodeAndEmail(trackingCode, email);
            model.addAttribute("feedback", feedback);
            model.addAttribute("replies", replyService.getRepliesByFeedback(feedback));
            model.addAttribute("email", email);
            return "public/ticket-detail";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/view-ticket";
        }
    }
}
