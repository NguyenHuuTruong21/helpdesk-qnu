package edu.qnu.helpdesk.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.qnu.helpdesk.entity.User;
import edu.qnu.helpdesk.exception.BadRequestException;
import edu.qnu.helpdesk.repository.UserRepository;
import edu.qnu.helpdesk.service.EmailService;
import edu.qnu.helpdesk.service.SelfService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service 
@RequiredArgsConstructor 
@Slf4j
public class SelfServiceImpl implements SelfService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final edu.qnu.helpdesk.repository.FeedbackRepository feedbackRepository;

    @Override
    public Map<String, Object> lookupAccount(String mssv) {
        log.info("Bắt đầu tra cứu MSSV: {}", mssv); 
        // Tìm user có username = MSSV
        Optional<User> userOpt = userRepository.findByUsername(mssv);
        
        Map<String, Object> result = new HashMap<>();
        result.put("mssv", mssv);

        if (userOpt.isEmpty()) {
            log.warn("Không tìm thấy MSSV: {}", mssv); // Thêm dòng này
            result.put("status", "NOT_FOUND");
            result.put("message", "Không tìm thấy thông tin sinh viên với MSSV này.");
            return result;
        }

        User user = userOpt.get();
        log.info("Tìm thấy user: {}", user.getFullName());
        
        // 1. Kiểm tra Email trường
        boolean hasSchoolEmail = user.getEmail() != null && user.getEmail().endsWith("@st.qnu.edu.vn");
        result.put("hasSchoolEmail", hasSchoolEmail);
        if (hasSchoolEmail) {
            result.put("schoolEmail", maskEmail(user.getEmail()));
        }

        // 2. Kiểm tra thông tin liên lạc (SĐT & Email cá nhân)
        boolean hasPhone = user.getPhone() != null && !user.getPhone().isBlank();
        boolean hasPersonalEmail = user.getPersonalEmail() != null && !user.getPersonalEmail().isBlank();
        
        result.put("hasPhone", hasPhone);
        result.put("hasPersonalEmail", hasPersonalEmail);
        
        if (hasPhone) result.put("phone", maskPhone(user.getPhone()));
        if (hasPersonalEmail) result.put("personalEmail", maskEmail(user.getPersonalEmail()));

        // 3. Đưa ra gợi ý dịch vụ
        if (hasSchoolEmail) {
            result.put("suggestion", "Bạn đã có Email trường. Nếu quên mật khẩu, hãy dùng Dịch vụ 2.");
            result.put("action", "RESET_PASSWORD");
        } else if (!hasPhone || !hasPersonalEmail) {
            result.put("suggestion", "Bạn chưa có Email trường và đang thiếu thông tin liên lạc. Vui lòng bổ sung thông tin (Dịch vụ 4) trước khi xin cấp mới.");
            result.put("action", "UPDATE_INFO");
        } else {
            result.put("suggestion", "Bạn chưa có Email trường. Bạn có thể yêu cầu cấp mới ngay bây giờ.");
            result.put("action", "CREATE_EMAIL");
        }

        return result;
    }
    // Hàm che mờ dữ liệu
    private String maskEmail(String email) {
        if (email == null) return "";
        int atIndex = email.indexOf("@");
        if (atIndex < 0) return "***";
        if (atIndex <= 2) return "***" + email.substring(atIndex);
        return email.substring(0, 2) + "***" + email.substring(atIndex);
    }
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 5) return "***";
        return "***" + phone.substring(phone.length() - 3);
    }
    @Override 
    @Transactional 
    public String resetPassword(String mssv, String citizenId){
        // 1. Tìm user theo MSSV
        User user = userRepository.findByUsername(mssv).orElseThrow(() -> new BadRequestException("Không tìm thấy sinh viên với MSSV này"));
        // 2. Đối soát CCCD
        if (user.getCitizenId() == null || !user.getCitizenId().equals(citizenId)) {
            throw new BadRequestException("Số CCCD/CMND không khớp với hồ sơ. Vui lòng kiểm tra lại.");
        }
        // 3. Kiểm tra có Email cá nhân chưa
        if(user.getPersonalEmail() == null || user.getPersonalEmail().isBlank()) {
            throw new BadRequestException("Tài khoản chưa cập nhật Email cá nhân. Vui lòng liên hệ Phòng CTSV.");
        }
        // 4. Tạo mật khẩu ngẫu nhiên (Ví dụ: Qnu@123456)
        String newPassword = "Qnu@" + (int)(Math.random() * 900000 + 100000);

        // 5. Cập nhật vào DB
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        // 6. Gửi email cá nhân
        String emailBody = "Chào " + user.getFullName() + ",\n\n" +
                "Mật khẩu mới cho tài khoản Email trường của bạn là: " + newPassword + "\n" +
                "Vui lòng đăng nhập và đổi mật khẩu ngay sau khi nhận được email này.\n\n" +
                "Trân trọng,\nTrung tâm Số & Học liệu - Đại học Quy Nhơn";
        
        emailService.sendNewPasswordEmail(
        user.getPersonalEmail(), 
        user.getFullName(), 
        user.getUsername(), 
        newPassword
);

        return user.getPersonalEmail();
    }
    
    @Override
    @Transactional
    public void requestCreateEmail(edu.qnu.helpdesk.dto.CreateEmailRequest request) {
        // 1. Tìm user
        User user = userRepository.findByUsername(request.getStudentId())
                .orElseThrow(() -> new BadRequestException(
                        "Không tìm thấy sinh viên với MSSV này trong hệ thống."));

        // 2. Kiểm tra đã có email trường chưa
        if (user.getEmail() != null && user.getEmail().endsWith("@st.qnu.edu.vn")) {
            throw new BadRequestException(
                    "Tài khoản đã có Email trường. Vui lòng không gửi yêu cầu cấp mới.");
        }

        // 3. Tự động cập nhật thông tin liên lạc vào hồ sơ
        user.setPhone(request.getPhone());
        user.setPersonalEmail(request.getPersonalEmail());
        user.setFullName(request.getFullName());
        userRepository.save(user);

        // 4. Tạo Ticket tự động gửi đến Admin (Trung tâm Số & Học liệu)
        edu.qnu.helpdesk.entity.Feedback ticket = edu.qnu.helpdesk.entity.Feedback.builder()
                .user(user)
                .isGuest(false)
                .title("[Yêu cầu cấp mới Email] MSSV " + request.getStudentId())
                .category(edu.qnu.helpdesk.enums.FeedbackCategory.EMAIL_ACCOUNT)
                .content(String.format("""
                        Sinh viên yêu cầu cấp mới tài khoản Email trường.
                        
                        - Họ tên: %s
                        - MSSV: %s
                        - SĐT: %s
                        - Email cá nhân: %s
                        
                        Hệ thống đã kiểm tra: sinh viên CHƯA có Email trường, thông tin liên lạc đã đầy đủ.
                        Đề nghị TT.S&HL tạo tài khoản Email và phản hồi cho sinh viên.
                        """,
                        request.getFullName(),
                        request.getStudentId(),
                        request.getPhone(),
                        request.getPersonalEmail()
                ))
                .status(edu.qnu.helpdesk.enums.FeedbackStatus.PENDING)
                .priority(2)
                .trackingCode("QNU-" + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"))
                        + "-" + String.format("%06d", (int)(Math.random() * 999999)))
                .build();

        feedbackRepository.save(ticket);
    }
    @Override
    @Transactional
    public void updateContactInfo(edu.qnu.helpdesk.dto.UpdateInfoRequest request) {
        // 1. Tìm user
        User user = userRepository.findByUsername(request.getStudentId())
                .orElseThrow(() -> new BadRequestException(
                        "Không tìm thấy sinh viên với MSSV này."));

        // 2. Đối soát CCCD
        if (user.getCitizenId() == null || !user.getCitizenId().equals(request.getCitizenId())) {
            throw new BadRequestException(
                    "Số CCCD/CMND không khớp với hồ sơ sinh viên.");
        }

        // 3. Kiểm tra có thay đổi gì không
        boolean phoneChanged = request.getNewPhone() != null
                && !request.getNewPhone().equals(user.getPhone());
        boolean emailChanged = request.getNewPersonalEmail() != null
                && !request.getNewPersonalEmail().equals(user.getPersonalEmail());

        if (!phoneChanged && !emailChanged) {
            throw new BadRequestException(
                    "Không có thông tin nào thay đổi. Vui lòng nhập thông tin mới.");
        }

        // 4. Cập nhật
        if (phoneChanged) user.setPhone(request.getNewPhone());
        if (emailChanged) user.setPersonalEmail(request.getNewPersonalEmail());
        userRepository.save(user);

        // 5. Tạo ticket thông báo cho phòng CTSV
        StringBuilder changes = new StringBuilder();
        if (phoneChanged) changes.append("- SĐT mới: ").append(request.getNewPhone()).append("\n");
        if (emailChanged) changes.append("- Email cá nhân mới: ").append(request.getNewPersonalEmail()).append("\n");

        edu.qnu.helpdesk.entity.Feedback ticket = edu.qnu.helpdesk.entity.Feedback.builder()
                .user(user)
                .isGuest(false)
                .title("[Cập nhật thông tin] MSSV " + request.getStudentId())
                .category(edu.qnu.helpdesk.enums.FeedbackCategory.KHAC)
                .content(String.format("""
                        Sinh viên yêu cầu cập nhật thông tin liên lạc.
                        
                        - MSSV: %s
                        - Họ tên: %s
                        
                        Nội dung thay đổi:
                        %s
                        
                        Đề nghị Phòng CTSV kiểm tra và cập nhật vào hệ thống UIS.
                        """,
                        request.getStudentId(),
                        user.getFullName(),
                        changes.toString()
                ))
                .status(edu.qnu.helpdesk.enums.FeedbackStatus.PENDING)
                .priority(1)
                .trackingCode("QNU-" + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"))
                        + "-" + String.format("%06d", (int)(Math.random() * 999999)))
                .build();

        feedbackRepository.save(ticket);
    }
}
