package edu.qnu.helpdesk.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import edu.qnu.helpdesk.enums.FeedbackCategory;
import edu.qnu.helpdesk.enums.FeedbackStatus;
import jakarta.persistence.*;
import lombok.*;

/*
    Phản hồi / yêu cầu hỗ trợ do người dùng gửi
 */
@Entity
@Table(name = "feedbacks", indexes = {
                @Index(name = "idx_feedback_user", columnList = "user_id"),
                @Index(name = "idx_feedback_status", columnList = "status"),
                @Index(name = "idx_feedback_tracking", columnList = "tracking_code", unique = true),
                @Index(name = "idx_feedback_guest_email", columnList = "guest_email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Feedback {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        // ============ MÃ TRA CỨU (cho guest) ============
        @Column(name = "tracking_code", unique = true, length = 30)
        private String trackingCode;

        // ============ NGƯỜI GỬI (có thể null nếu guest) ============
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_feedback_user"))
        private User user;

        // ============ THÔNG TIN GUEST ============
        @Column(name = "guest_name", length = 100)
        private String guestName;

        @Column(name = "guest_email", length = 100)
        private String guestEmail;

        @Column(name = "guest_phone", length = 20)
        private String guestPhone;

        @Column(name = "is_guest", nullable = false)
        @Builder.Default
        private Boolean isGuest = false;

        // ============ NỘI DUNG ============
        @Column(name = "title", nullable = false, length = 200)
        private String title;

        @Enumerated(EnumType.STRING)
        @Column(name = "category", length = 50)
        private FeedbackCategory category;

        @Lob
        @Column(name = "content", nullable = false, columnDefinition = "TEXT")
        private String content;

        @Column(name = "attachment", length = 255)
        private String attachment;

        @Column(name = "attachment_name", length = 255)
        private String attachmentName;

        @Enumerated(EnumType.STRING)
        @Column(name = "status", nullable = false, length = 20)
        @Builder.Default
        private FeedbackStatus status = FeedbackStatus.PENDING;

        @Column(name = "priority")
        @Builder.Default
        private Integer priority = 2;

        @Column(name = "created_at", nullable = false, updatable = false)
        @Builder.Default
        private LocalDateTime createdAt = LocalDateTime.now();

        @Column(name = "updated_at")
        private LocalDateTime updatedAt;

        @OneToMany(mappedBy = "feedback", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
        @OrderBy("createdAt ASC")
        @Builder.Default
        private List<Reply> replies = new ArrayList<>();

        @Column(name = "appointment_room", length = 100)
        private String appointmentRoom;

        @Column(name = "appointment_time")
        private LocalDateTime appointmentTime;

        @Column(name = "appointment_staff", length = 100)
        private String appointmentStaff;

        // ============ HELPER ============

        @Transient
        public String getDisplayName() {
                if (user != null)
                        return user.getFullName();
                return guestName != null ? guestName : "Khách";
        }

        @Transient
        public String getDisplayEmail() {
                if (user != null)
                        return user.getEmail();
                return guestEmail;
        }

        @PreUpdate
        public void onUpdate() {
                this.updatedAt = LocalDateTime.now();
        }
}