package edu.qnu.helpdesk.service;

public interface EmailService {
    /* Gửi email chứa link đặt lại mật khẩu */
    void sendPasswordResetEmail(String toEmail, String subject, String customBody);
    /* Gửi email nhắc lại tên đăng nhập */
    void sendUsernameReminderEmail(String toEmail, String username);
    /* Gửi email thông báo có yêu cầu liên hệ mới */
    void sendContactNotificationToAdmin(String adminEmail, String userFullName,
                                        String userEmail, String issueType, String content);
    /* */
    void sendNewPasswordEmail(String toEmail, String fullName, String mssv, String newPassword);
}
