package edu.qnu.helpdesk.config;

import edu.qnu.helpdesk.entity.Feedback;
import edu.qnu.helpdesk.entity.Reply;
import edu.qnu.helpdesk.entity.User;
import edu.qnu.helpdesk.enums.FeedbackCategory;
import edu.qnu.helpdesk.enums.FeedbackStatus;
import edu.qnu.helpdesk.enums.Role;
import edu.qnu.helpdesk.repository.FeedbackRepository;
import edu.qnu.helpdesk.repository.ReplyRepository;
import edu.qnu.helpdesk.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Khởi tạo dữ liệu mặc định khi ứng dụng chạy lần đầu:
 *  - Tài khoản admin & user demo
 *  - Một số ticket mẫu để test giao diện
 *
 * Chỉ chạy khi database trống (idempotent).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final FeedbackRepository feedbackRepository;
    private final ReplyRepository replyRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("========== DATA INITIALIZER ==========");

        User admin = createDefaultAdmin();
        User user = createDefaultUser();

        createSampleTickets(user, admin);

        log.info("========== HOÀN TẤT KHỞI TẠO ==========");
    }

    // ============================================================
    // TÀI KHOẢN MẶC ĐỊNH
    // ============================================================

    private User createDefaultAdmin() {
        return userRepository.findByUsername("admin").orElseGet(() -> {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .fullName("Quản trị viên hệ thống")
                    .email("admin@qnu.edu.vn")
                    .phone("02563845845")
                    .role(Role.ADMIN)
                    .enabled(true)
                    .build();
            User saved = userRepository.save(admin);
            log.info(">>> [ADMIN] Đã tạo tài khoản: admin / admin123");
            return saved;
        });
    }

    private User createDefaultUser() {
        return userRepository.findByUsername("sv01").orElseGet(() -> {
            User user = User.builder()
                    .username("sv01")
                    .password(passwordEncoder.encode("123456"))
                    .fullName("Nguyễn Văn A")
                    .email("sv01@qnu.edu.vn")
                    .phone("0123456789")
                    .role(Role.USER)
                    .enabled(true)
                    .build();
            User saved = userRepository.save(user);
            log.info(">>> [USER] Đã tạo tài khoản: sv01 / 123456");
            return saved;
        });
    }

    // ============================================================
    // TICKET MẪU
    // ============================================================

    private void createSampleTickets(User user, User admin) {
        // Chỉ seed khi chưa có ticket nào
        if (feedbackRepository.count() > 0) {
            log.info(">>> Đã có dữ liệu ticket, bỏ qua bước seed mẫu.");
            return;
        }

        log.info(">>> Đang tạo dữ liệu ticket mẫu...");

        // ============================================================
        // TICKET 1: USER gửi - đã được admin trả lời (RESOLVED)
        // ============================================================
        Feedback ticket1 = Feedback.builder()
                .trackingCode("QNU-20250115-ABC123")
                .user(user)
                .isGuest(false)
                .title("Không đăng nhập được Wifi KTX")
                .category(FeedbackCategory.WIFI_ACCOUNT)
                .content("""
                        Em chào thầy/cô,
                        
                        Tài khoản Wifi của em bị báo sai mật khẩu từ hôm qua (14/01).
                        Em đã thử đăng nhập nhiều lần nhưng không được.
                        
                        Thông tin sinh viên:
                        - Họ tên: Nguyễn Văn A
                        - MSSV: 12345678
                        - Lớp: CNTT K19
                        - KTX: Phòng B203
                        
                        Mong thầy/cô hỗ trợ cấp lại mật khẩu giúp em.
                        Em xin cảm ơn!
                        """)
                .status(FeedbackStatus.RESOLVED)
                .priority(2)
                .createdAt(LocalDateTime.now().minusDays(3))
                .build();
        Feedback savedTicket1 = feedbackRepository.save(ticket1);

        // Admin trả lời ticket 1
        Reply reply1 = Reply.builder()
                .feedback(savedTicket1)
                .admin(admin)
                .content("""
                        Chào em Nguyễn Văn A,
                        
                        Phòng ICT đã kiểm tra và cấp lại mật khẩu Wifi cho em.
                        Mật khẩu mới: Qnu@2025#K19
                        
                        Em vui lòng:
                        1. Ngắt kết nối Wifi hiện tại
                        2. Chọn mạng "QNU-Student" và đăng nhập lại
                        3. Đổi mật khẩu mới ngay sau khi đăng nhập lần đầu
                        
                        Nếu vẫn gặp vấn đề, em có thể đến trực tiếp Phòng ICT
                        (Tầng 2, nhà A) để được hỗ trợ.
                        
                        Trân trọng,
                        Phòng ICT
                        """)
                .createdAt(LocalDateTime.now().minusDays(2))
                .build();
        replyRepository.save(reply1);

        log.info(">>> [TICKET 1] User ticket đã resolve: QNU-20250115-ABC123");

        // ============================================================
        // TICKET 2: GUEST gửi - ĐANG CHỜ XỬ LÝ (PENDING)
        // ============================================================
        Feedback ticket2 = Feedback.builder()
                .trackingCode("QNU-20250116-XYZ789")
                .user(null)
                .isGuest(true)
                .guestName("Trần Thị B")
                .guestEmail("tranthib@gmail.com")
                .guestPhone("0987654321")
                .title("Quên mật khẩu email Office 365")
                .category(FeedbackCategory.EMAIL_ACCOUNT)
                .content("""
                        Em chào thầy/cô,
                        
                        Em là sinh viên K20. Em bị quên mật khẩu email Office 365
                        do lâu ngày không đăng nhập (khoảng 3 tháng).
                        
                        Thông tin của em:
                        - Họ tên: Trần Thị B
                        - MSSV: 87654321
                        - Email: b.tran@student.qnu.edu.vn
                        - SĐT: 0987654321
                        
                        Em muốn nhờ thầy/cô reset lại mật khẩu để em có thể
                        truy cập lại hộp thư và hệ thống học tập.
                        
                        Em xin cảm ơn!
                        """)
                .status(FeedbackStatus.PENDING)
                .priority(2)
                .createdAt(LocalDateTime.now().minusHours(5))
                .build();
        feedbackRepository.save(ticket2);

        log.info(">>> [TICKET 2] Guest ticket pending: QNU-20250116-XYZ789");

        // ============================================================
        // TICKET 3: USER gửi - ĐANG XỬ LÝ (PROCESSING)
        // ============================================================
        Feedback ticket3 = Feedback.builder()
                .trackingCode("QNU-20250117-DEF456")
                .user(user)
                .isGuest(false)
                .title("Không nộp được bài tập trên QES E-learning")
                .category(FeedbackCategory.QES_ELEARNING)
                .content("""
                        Kính gửi Phòng Đào tạo,
                        
                        Hôm nay em vào hệ thống QES để nộp bài tập môn "Lập trình Java"
                        cho thầy Trần Văn C nhưng khi nhấn nút "Nộp bài" thì hệ thống
                        báo lỗi "File upload failed - Error 500".
                        
                        Em đã thử:
                        - Đổi trình duyệt (Chrome, Firefox, Edge)
                        - Đổi máy tính khác
                        - Đổi mạng Wifi sang 4G
                        
                        Nhưng vẫn không nộp được. Deadline nộp bài là 23:59 hôm nay (17/01).
                        Mong thầy/cô hỗ trợ gấp để em không bị trễ hạn.
                        
                        Em xin cảm ơn!
                        - Nguyễn Văn A - MSSV 12345678
                        """)
                .status(FeedbackStatus.PROCESSING)
                .priority(3)
                .createdAt(LocalDateTime.now().minusHours(2))
                .build();
        feedbackRepository.save(ticket3);

        log.info(">>> [TICKET 3] User ticket processing: QNU-20250117-DEF456");

        log.info(">>> Đã tạo 3 ticket mẫu thành công!");
    }
}