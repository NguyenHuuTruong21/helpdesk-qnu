package edu.qnu.helpdesk.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import edu.qnu.helpdesk.enums.Role;
import jakarta.persistence.*;
import lombok.*;

/*
    Dây là người dùng (sinh viên, giảng viên, admin)
 */
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_username", columnList = "username", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    @Builder.Default
    private Role role = Role.USER;

    @Column(name = "enabled", nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Quan hệ 1-N: Một user có nhiều phản hồi
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<Feedback> feedbacks = new ArrayList<>();

    // Quan hệ 1-N: Một admin có nhiều reply
    @OneToMany(mappedBy = "admin", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Reply> replies = new ArrayList<>();
    
    @Column(name = "citizen_id", length = 20)
    private String citizenId;

    @Column(name = "personal_email", length = 100)
    private String personalEmail;

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
