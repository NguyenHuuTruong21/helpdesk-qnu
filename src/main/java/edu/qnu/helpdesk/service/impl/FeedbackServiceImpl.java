package edu.qnu.helpdesk.service.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.qnu.helpdesk.dto.FeedbackRequest;
import edu.qnu.helpdesk.entity.Feedback;
import edu.qnu.helpdesk.entity.User;
import edu.qnu.helpdesk.enums.FeedbackStatus;
import edu.qnu.helpdesk.exception.BadRequestException;
import edu.qnu.helpdesk.exception.ResourceNotFoundException;
import edu.qnu.helpdesk.repository.FeedbackRepository;
import edu.qnu.helpdesk.repository.UserRepository;
import edu.qnu.helpdesk.service.FeedbackService;
import edu.qnu.helpdesk.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {
    private final FeedbackRepository feedbackRepository;
    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;

    // User
    @Override
    @Transactional
    public Feedback createFeedback(FeedbackRequest request, User user) {
        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new BadRequestException("Nội dung không được để trống");
        }

        String storedFileName = null;
        String originalFileName = null;

        if (request.getAttachment() != null && !request.getAttachment().isEmpty()) {
            storedFileName = fileStorageService.storeFile(request.getAttachment());
            originalFileName = request.getAttachment().getOriginalFilename();
        }

        Feedback feedback = Feedback.builder()
                .user(user)
                .title(request.getTitle())
                .category(request.getCategory())
                .content(request.getContent())
                .priority(request.getPriority() != null ? request.getPriority() : 2)
                .attachment(storedFileName)
                .attachmentName(originalFileName)
                .status(FeedbackStatus.PENDING)
                .build();

        Feedback saved = feedbackRepository.save(feedback);
        log.info("User [{}] created feedback [{}]", user.getUsername(), saved.getId());
        return saved;
    }

    @Override
    public List<Feedback> getFeedbacksByUser(User user) {
        return feedbackRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Override
    public Page<Feedback> getFeedbacksByUser(User user, Pageable pageable) {
        return feedbackRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }

    @Override
    public Feedback getFeedbackByIdAndUser(Long id, User user) {
        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Phản hồi", id));
        if (!feedback.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Bạn không có quyền xem phản hồi này");
        }
        return feedback;
    }

    // Admin
    @Override
    public Page<Feedback> getAllFeedbacks(Pageable pageable) {
        return feedbackRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    @Override
    public Page<Feedback> filterFeedbacks(FeedbackStatus status, String keyword, Pageable pageable) {
        String kw = (keyword == null) ? "" : keyword.trim();
        return feedbackRepository.filter(status, kw, pageable);
    }

    @Override
    public Feedback getFeedbackById(Long id) {
        return feedbackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Phản hồi", id));
    }

    @Override
    @Transactional
    public void updateStatus(Long id, FeedbackStatus status) {
        Feedback feedback = getFeedbackById(id);
        feedback.setStatus(status);
        feedbackRepository.save(feedback);
        log.info("Feedback [{}] status updated to {}", id, status);
    }

    @Override
    @Transactional
    public void approveCitizenIdUpdate(Long id) {
        Feedback feedback = getFeedbackById(id);
        if (feedback.getRequestedCitizenId() == null || feedback.getRequestedCitizenId().isBlank()) {
            throw new BadRequestException("Yêu cầu này không có thông tin CCCD cần duyệt.");
        }
        if (feedback.isCitizenIdUpdateApproved()) {
            throw new BadRequestException("Yêu cầu cập nhật CCCD đã được duyệt trước đó.");
        }
        User user = feedback.getUser();
        if (user == null) {
            throw new BadRequestException("Không thể cập nhật CCCD vì yêu cầu chưa gắn với tài khoản.");
        }

        user.setCitizenId(feedback.getRequestedCitizenId());
        userRepository.save(user);
        feedback.setCitizenIdUpdateApproved(true);
        feedbackRepository.save(feedback);
        log.info("Approved CCCD update for feedback [{}] and user [{}]", id, user.getUsername());
    }

    @Override
    @Transactional
    public void deleteFeedback(Long id) {
        Feedback feedback = getFeedbackById(id);
        if (feedback.getAttachment() != null) {
            fileStorageService.deleteFile(feedback.getAttachment());
        }
        feedbackRepository.delete(feedback);
        log.info("Deleted feedback [{}]", id);
    }

    // Stats
    @Override
    public long countAll() {
        return feedbackRepository.count();
    }

    @Override
    public long countByStatus(FeedbackStatus status) {
        return feedbackRepository.countByStatus(status);
    }

    @Override
    public long countByUser(User user) {
        return feedbackRepository.countByUser(user);
    }

    @Override
    public long countByUserAndStatus(User user, FeedbackStatus status) {
        return feedbackRepository.countByUserAndStatus(user, status);
    }

    @Override
    @Transactional
    public Feedback save(Feedback feedback) {
        return feedbackRepository.save(feedback);
    }
}
