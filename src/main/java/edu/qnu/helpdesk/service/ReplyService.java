package edu.qnu.helpdesk.service;

import java.util.List;

import edu.qnu.helpdesk.dto.ReplyRequest;
import edu.qnu.helpdesk.entity.Feedback;
import edu.qnu.helpdesk.entity.Reply;
import edu.qnu.helpdesk.entity.User;

public interface ReplyService {
    Reply createReply(Long feedbackId, ReplyRequest request, User admin);

    List<Reply> getRepliesByFeedback(Feedback feedback);

    long countByFeedback(Feedback feedback);

    void createUserReply(Feedback feedback, String message);
}
