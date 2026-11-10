package edu.qnu.helpdesk.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

/*
    Phản hồi của admin đối với một Feedback
 */
@Entity
@Table(name = "replies", indexes = {
        @Index(name = "idx_reply_feedback", columnList = "feedback_id"),
        @Index(name = "idx_reply_admin", columnList = "admin_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Thuộc feedback nào
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "feedback_id", nullable = false, foreignKey = @ForeignKey(name = "fk_reply_feedback"))
    private Feedback feedback;

    // Admin nào trả lời
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "admin_id", nullable = true, foreignKey = @ForeignKey(name = "fk_reply_admin"))
    private User admin;

    @Lob
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
