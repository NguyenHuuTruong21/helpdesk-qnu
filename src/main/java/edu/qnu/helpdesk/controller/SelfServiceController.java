package edu.qnu.helpdesk.controller;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import edu.qnu.helpdesk.dto.AccountLookupRequest;
import edu.qnu.helpdesk.dto.CreateEmailRequest;
import edu.qnu.helpdesk.dto.GuestTicketRequest;
import edu.qnu.helpdesk.dto.ResetPasswordRequest;
import edu.qnu.helpdesk.dto.UpdateInfoRequest;
import edu.qnu.helpdesk.entity.Feedback;
import edu.qnu.helpdesk.enums.FeedbackCategory;
import edu.qnu.helpdesk.exception.BadRequestException;
import edu.qnu.helpdesk.service.GuestTicketService;
import edu.qnu.helpdesk.service.SelfService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller 
@RequestMapping("dich-vu")
@RequiredArgsConstructor 
public class SelfServiceController {
    private final SelfService selfService;
    private final GuestTicketService guestTicketService;

    // --- Dịch vụ 1: Tra cứu ---
    @GetMapping("/tra-cuu")
    public String showLookupForm(Model model) {
        if (!model.containsAttribute("request")) {
            model.addAttribute("request", new AccountLookupRequest());
        }
        return "public/service-lookup";
    }
    @PostMapping("/tra-cuu")
    public String processLookup(@Valid @ModelAttribute("request") AccountLookupRequest request,
                                BindingResult bindingResult, Model model) {
        // 1. Nếu có lỗi nhập liệu (VD: nhập chữ thay vì số) -> Trả về form
        if (bindingResult.hasErrors()) {
            return "public/service-lookup";
        }
        
        try {
            // 2. Gọi service tra cứu
            Map<String, Object> result = selfService.lookupAccount(request.getStudentId());
            
            // 3. Truyền dữ liệu ra giao diện
            model.addAttribute("result", result);
            model.addAttribute("mssv", request.getStudentId());
            
        } catch (Exception e) {
            // 4. Bắt lỗi (nếu có) và hiển thị thông báo thay vì văng lỗi 500
            model.addAttribute("errorMessage", "Có lỗi xảy ra trong quá trình tra cứu: " + e.getMessage());
        }
        
        return "public/service-lookup";
    }
    // --- Dịch vụ 2: Quên mật khẩu ---
    @GetMapping("/quen-mat-khau")
    public String showResetForm(Model model) {
        if (!model.containsAttribute("request")) {
            model.addAttribute("request", new ResetPasswordRequest());
        }
        return "public/service-reset-password";
    }

    @PostMapping("/quen-mat-khau")
    public String processReset(@Valid @ModelAttribute("request") ResetPasswordRequest request,
                            BindingResult bindingResult, Model model, 
                            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "public/service-reset-password";
        }
        try {
            String sentEmail = selfService.resetPassword(request.getStudentId(), request.getCitizenId());
            redirectAttributes.addFlashAttribute("successMessage",
                "Mật khẩu mới đã được gửi về Email cá nhân (" + sentEmail + "). Vui lòng kiểm tra hộp thư!");
            return "redirect:/dich-vu/quen-mat-khau";
        } catch (BadRequestException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "public/service-reset-password";
        }
    }
    // === DỊCH VỤ 3: CẤP MỚI EMAIL ====
    @GetMapping("/cap-moi-email")
    public String showCreateEmailForm(Model model) {
        if (!model.containsAttribute("request")) {
            model.addAttribute("request", new CreateEmailRequest());
        }
        return "public/service-create-email";
    }

    @PostMapping("/cap-moi-email")
    public String processCreateEmail(@Valid @ModelAttribute("request") CreateEmailRequest request,
                                    BindingResult bindingResult, Model model,
                                    RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "public/service-create-email";
        }
        try {
            selfService.requestCreateEmail(request);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Yêu cầu cấp mới Email đã được gửi đến TT.S&HL. " +
                    "Vui lòng chờ phản hồi qua Email cá nhân.");
            return "redirect:/dich-vu/cap-moi-email/success";
        } catch (BadRequestException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "public/service-create-email";
        }
    }

    @GetMapping("/cap-moi-email/success")
    public String showCreateEmailSuccess() {
        return "public/service-create-email-success";
    }

    // ============ DỊCH VỤ 4: CẬP NHẬT THÔNG TIN ============
    @GetMapping("/cap-nhat-thong-tin")
    public String showUpdateInfoForm(Model model) {
        if (!model.containsAttribute("request")) {
            model.addAttribute("request", new UpdateInfoRequest());
        }
        return "public/service-update-info";
    }

    @PostMapping("/cap-nhat-thong-tin")
    public String processUpdateInfo(@Valid @ModelAttribute("request") UpdateInfoRequest request,
                                    BindingResult bindingResult, Model model,
                                    RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "public/service-update-info";
        }
        try {
            selfService.updateContactInfo(request);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Cập nhật thông tin thành công! Yêu cầu đã được gửi đến Phòng CTSV.");
            return "redirect:/dich-vu/cap-nhat-thong-tin";
        } catch (BadRequestException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "public/service-update-info";
        }
    }
    // --- Dịch vụ 5: Báo cáo sự cố kỹ thuật ---
    @GetMapping("/bao-cao-su-co")
    public String showIncidentForm(Model model) {
        if (!model.containsAttribute("request")) {
            model.addAttribute("request", new GuestTicketRequest()); // Tái sử dụng DTO Guest
        }
        // Chỉ hiển thị các danh mục sự cố kỹ thuật
        model.addAttribute("categories", new FeedbackCategory[]{
            FeedbackCategory.OTP_2FA, 
            FeedbackCategory.ACCOUNT_HACKED, 
            FeedbackCategory.SYSTEM_ERROR,
            FeedbackCategory.WIFI_ACCOUNT,
            FeedbackCategory.EMAIL_ACCOUNT
        });
        return "public/service-incident";
    }
    @PostMapping("/bao-cao-su-co")
    public String processIncident(@Valid @ModelAttribute("request") GuestTicketRequest request,
                                  BindingResult bindingResult, 
                                  RedirectAttributes redirectAttributes, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", FeedbackCategory.values());
            return "public/service-incident";
        }
        try {
            // Gọi service tạo ticket (dùng lại hàm của GuestTicketService)
            Feedback feedback = guestTicketService.createGuestTicket(request);
            redirectAttributes.addFlashAttribute("trackingCode", feedback.getTrackingCode());
            redirectAttributes.addFlashAttribute("guestEmail", request.getEmail());
            return "redirect:/dich-vu/bao-cao-su-co/thanh-cong";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "public/service-incident";
        }
    }
     @GetMapping("/bao-cao-su-co/thanh-cong")
    public String showIncidentSuccess() {
        return "public/service-incident-success";
    }
}
