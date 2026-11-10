package edu.qnu.helpdesk.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import edu.qnu.helpdesk.dto.RegisterRequest;
import edu.qnu.helpdesk.exception.BadRequestException;
import edu.qnu.helpdesk.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    // Hiển thị trang đăng nhập
    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            @RequestParam(value = "registered", required = false) String registered,
            Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Tên đăng nhập hoặc mật khẩu không đúng");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "Bạn đã đăng xuất thành công");
        }
        if (registered != null) {
            model.addAttribute("successMessage", "Đăng ký thành công! Vui lòng đăng nhập.");
        }
        return "auth/login";
    }

    // Hiển thị form dắng ký
    @GetMapping("/register")
    public String registerPage(Model model) {
        if (!model.containsAttribute("registerRequest")) {
            model.addAttribute("registerRequest", new RegisterRequest());
        }
        return "auth/register";
    }

    // Xử lý đăng ký
    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerRequest") RegisterRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        // 1. Kiểm tra lỗi validation từ form
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        // 2. Kiểm tra mật khẩu xác nhận
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            model.addAttribute("errorMessage", "Mật khẩu xác nhận không khớp");
            return "auth/register";
        }

        // 3. Gọi service đăng ký
        try {
            userService.register(request);
            log.info("Đăng ký thành công user: {}", request.getUsername());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đăng ký thành công! Hãy đăng nhập để tiếp tục.");
            return "redirect:/login?registered=true";
        } catch (BadRequestException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/register";
        } catch (Exception e) {
            log.error("Lỗi đăng ký: ", e);
            model.addAttribute("errorMessage", "Đã xảy ra lỗi, vui lòng thử lại sau.");
            return "auth/register";
        }
    }
}
