package edu.qnu.helpdesk.service.impl;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.qnu.helpdesk.dto.RegisterRequest;
import edu.qnu.helpdesk.entity.Feedback;
import edu.qnu.helpdesk.entity.User;
import edu.qnu.helpdesk.enums.Role;
import edu.qnu.helpdesk.exception.BadRequestException;
import edu.qnu.helpdesk.exception.ResourceNotFoundException;
import edu.qnu.helpdesk.repository.FeedbackRepository;
import edu.qnu.helpdesk.repository.UserRepository;
import edu.qnu.helpdesk.repository.UserRepository.UserSummary;
import edu.qnu.helpdesk.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final FeedbackRepository feedbackRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public User register(RegisterRequest request) {

        // 1. Kiểm tra mật khẩu xác nhận
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Mật khẩu xác nhận không khớp");
        }

        // 2. Kiểm tra username trùng
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Tên đăng nhập đã tồn tại");
        }

        String email = request.getEmail();
        if (email != null) {
            email = email.trim();
            if (email.isEmpty()) {
                email = null;
            } else {
                email = email.toLowerCase(Locale.ROOT);
            }
        }

        // 3. Kiểm tra email trùng (nếu có)
        if (email != null && userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email đã được sử dụng");
        }

        // 4. Tạo user mới
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .citizenId(request.getCitizenId() == null || request.getCitizenId().isBlank()
                        ? null
                        : request.getCitizenId().trim())
                .email(email)
                .phone(request.getPhone())
                .role(Role.USER)
                .enabled(true)
                .build();

        User saved = userRepository.save(user);
        claimGuestFeedback(saved);
        log.info("Registered new user: {}", saved.getUsername());
        return saved;
    }

    @Override
    @Transactional
    public void claimGuestFeedbackForUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user: " + username));
        claimGuestFeedback(user);
    }

    private void claimGuestFeedback(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            return;
        }

        String email = user.getEmail().trim().toLowerCase(Locale.ROOT);
        List<Feedback> guestFeedback = feedbackRepository.findUnclaimedGuestFeedbackByEmail(email);
        for (Feedback feedback : guestFeedback) {
            feedback.setUser(user);
            feedback.setIsGuest(false);
        }
        if (!guestFeedback.isEmpty()) {
            feedbackRepository.saveAll(guestFeedback);
            log.info("Linked {} guest feedback item(s) to user [{}]",
                    guestFeedback.size(), user.getUsername());
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", id));
    }

    @Override
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            throw new BadRequestException("Chưa đăng nhập");
        }
        String username = auth.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user: " + username));
    }

    @Override
    public List<User> findAllAdmins() {
        return userRepository.findByRole(Role.ADMIN);
    }

    @Override
    public Page<UserSummary> findUsers(Pageable pageable) {
        return userRepository.findByRole(Role.USER, pageable);
    }

    @Override
    public long countUsers() {
        return userRepository.countByRole(Role.USER);
    }

    @Override
    public long countAdmins() {
        return userRepository.countByRole(Role.ADMIN);
    }
}
