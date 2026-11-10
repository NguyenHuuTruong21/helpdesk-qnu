package edu.qnu.helpdesk.service.impl;

import edu.qnu.helpdesk.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    // =================================================================
    // 🔥 HÀM ĐÃ SỬA: Nhận Subject và Body tùy chỉnh
    // =================================================================
    @Override
    public void sendPasswordResetEmail(String toEmail, String subject, String customBody) {
        
        // Nếu bật mail thật (production), đoạn này sẽ gọi JavaMailSender
        if (mailEnabled) {
            // SimpleMailMessage message = new SimpleMailMessage();
            // message.setTo(toEmail);
            // message.setSubject(subject);
            // message.setText(customBody);
            // mailSender.send(message);
        }

        // Log ra console để dev/demo dễ nhìn
        log.info("""

                ================== 📧 EMAIL (TÙY CHỈNH) ==================
                To      : {}
                Subject : {}
                ------------------ Nội dung ------------------
                {}
                ==========================================================
                """, toEmail, subject, customBody);
    }

    @Override
    public void sendUsernameReminderEmail(String toEmail, String username) {
        String subject = "[HelpDesk QNU] Nhắc lại tên đăng nhập";
        String body = """
                Xin chào,
                
                Tên đăng nhập HelpDesk QNU của bạn là: %s
                
                Vui lòng đăng nhập tại: http://localhost:8080/login
                
                Trân trọng,
                Hệ thống HelpDesk QNU
                """.formatted(username);
        
        // Tái sử dụng hàm gửi chung
        sendPasswordResetEmail(toEmail, subject, body);
    }

    @Override
    public void sendContactNotificationToAdmin(String adminEmail, String userFullName,
                                                String userEmail, String issueType, String content) {
        String subject = "[HelpDesk QNU] Yêu cầu liên hệ mới - " + issueType;
        String body = """
                Có yêu cầu liên hệ mới từ người dùng:
                
                - Họ tên  : %s
                - Email   : %s
                - Loại    : %s
                - Nội dung: %s
                """.formatted(userFullName, userEmail, issueType, content);
        
        sendPasswordResetEmail(adminEmail, subject, body);
    }

        @Override
    public void sendNewPasswordEmail(String toEmail, String fullName, String mssv, String newPassword) {
        String subject = "[HelpDesk QNU] Cấp lại mật khẩu tài khoản Email trường";
        String body = """
                Xin chào %s (MSSV: %s),
                
                Hệ thống đã xác thực thông tin của bạn và tiến hành cấp lại mật khẩu.
                
                Mật khẩu mới của bạn là: %s
                
                VUI LÒNG LƯU Ý:
                - Đăng nhập vào Email trường (@st.qnu.edu.vn) bằng mật khẩu trên.
                - Thay đổi mật khẩu ngay sau khi đăng nhập lần đầu để bảo mật.
                - Không chia sẻ mật khẩu này cho bất kỳ ai.
                
                Nếu bạn không thực hiện yêu cầu này, hãy liên hệ ngay với Trung tâm Số & Học liệu (Phòng ICT).
                
                Trân trọng,
                Trung tâm Số & Học liệu - Trường Đại học Quy Nhơn
                """.formatted(fullName, mssv, newPassword);

        if (mailEnabled) {
            // Code gửi mail thật (nếu có cấu hình JavaMailSender)
            // mailSender.send(...)
        }

        log.info("""

                ================== 📧 EMAIL GIẢ LẬP (GỬI MẬT KHẨU) ==================
                To      : {}
                Subject : {}
                ------------------ Nội dung ------------------
                {}
                ======================================================================
                """, toEmail, subject, body);
    }
}