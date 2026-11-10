package edu.qnu.helpdesk.service;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import edu.qnu.helpdesk.dto.FeedbackRequest;
import edu.qnu.helpdesk.entity.Feedback;
import edu.qnu.helpdesk.entity.User;
import edu.qnu.helpdesk.enums.FeedbackStatus;

public interface FeedbackService {
    // User
    Feedback createFeedback(FeedbackRequest request, User user);

    List<Feedback> getFeedbacksByUser(User user);

    Page<Feedback> getFeedbacksByUser(User user, Pageable pageable);

    Feedback getFeedbackByIdAndUser(Long id, User user);

    // Admin
    Page<Feedback> getAllFeedbacks(Pageable pageable);

    Page<Feedback> filterFeedbacks(FeedbackStatus status, String keyword, Pageable pageable);

    Feedback getFeedbackById(Long id);

    void updateStatus(Long id, FeedbackStatus status);

    void deleteFeedback(Long id);
    
    // Statistics
    long countAll();

    long countByStatus(FeedbackStatus status);

    long countByUser(User user);

    long countByUserAndStatus(User user, FeedbackStatus status);

    Feedback save(Feedback feedback);
}
