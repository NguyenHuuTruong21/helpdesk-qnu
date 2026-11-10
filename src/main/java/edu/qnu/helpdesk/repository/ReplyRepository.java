package edu.qnu.helpdesk.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.qnu.helpdesk.entity.Feedback;
import edu.qnu.helpdesk.entity.Reply;

@Repository
public interface ReplyRepository extends JpaRepository<Reply, Long> {
    List<Reply> findByFeedbackOrderByCreatedAtAsc(Feedback feedback);

    long countByFeedback(Feedback feedback);

    void deleteByFeedback(Feedback feedback);
}
