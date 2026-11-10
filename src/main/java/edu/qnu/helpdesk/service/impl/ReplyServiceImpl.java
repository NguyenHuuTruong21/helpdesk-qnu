package edu.qnu.helpdesk.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.qnu.helpdesk.dto.ReplyRequest;
import edu.qnu.helpdesk.entity.Feedback;
import edu.qnu.helpdesk.entity.Reply;
import edu.qnu.helpdesk.entity.User;
import edu.qnu.helpdesk.exception.ResourceNotFoundException;
import edu.qnu.helpdesk.repository.FeedbackRepository;
import edu.qnu.helpdesk.repository.ReplyRepository;
import edu.qnu.helpdesk.service.ReplyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReplyServiceImpl implements ReplyService {
    private final ReplyRepository replyRepository;
    private final FeedbackRepository feedbackRepository;

    // Tạo reply
    @Override
    @Transactional
    public Reply createReply(Long feedbackId, ReplyRequest request, User admin) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new ResourceNotFoundException("Phản hồi", feedbackId));

        // Tạo reply
        Reply reply = Reply.builder()
                .feedback(feedback)
                .admin(admin)
                .content(request.getContent())
                .build();
        Reply saved = replyRepository.save(reply);

        // Cập nhật trạng thái feedback theo yêu cầu
        feedback.setStatus(request.getStatus());
        feedbackRepository.save(feedback);

        log.info("Admin [{}] replied to feedback [{}] - status: {}",
                admin.getUsername(), feedbackId, request.getStatus());
        return saved;
    }

    @Override
    public List<Reply> getRepliesByFeedback(Feedback feedback) {
        return replyRepository.findByFeedbackOrderByCreatedAtAsc(feedback);
    }

    @Override
    public long countByFeedback(Feedback feedback) {
        return replyRepository.countByFeedback(feedback);
    }

    @Override
    @Transactional
    public void createUserReply(Feedback feedback, String message) {
        if (message == null || message.isBlank()) return;
        
        // Tạo một Reply với admin = null để biểu thị đây là tin nhắn của User
        Reply reply = Reply.builder()
                .feedback(feedback)
                .admin(null) // User chat
                .content(message.trim())
                .build();
        replyRepository.save(reply);
        log.info("User replied to feedback {}", feedback.getId());
    }
}
