package edu.qnu.helpdesk.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import edu.qnu.helpdesk.entity.Feedback;
import edu.qnu.helpdesk.entity.User;
import edu.qnu.helpdesk.enums.FeedbackStatus;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
        // ==== Dành cho User ====
        List<Feedback> findByUserOrderByCreatedAtDesc(User user);

        Page<Feedback> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

        long countByUser(User user);

        long countByUserAndStatus(User user, FeedbackStatus status);

        // ==== Dành cho Admin ====
        List<Feedback> findAllByOrderByCreatedAtDesc();

        Page<Feedback> findAllByOrderByCreatedAtDesc(Pageable pageable);

        Page<Feedback> findByStatusOrderByCreatedAtDesc(FeedbackStatus status, Pageable pageable);

        long countByStatus(FeedbackStatus status);

        // ============ TRACKING CHO GUEST ============
        Optional<Feedback> findByTrackingCode(String trackingCode);

        Optional<Feedback> findByTrackingCodeAndGuestEmail(String trackingCode, String guestEmail);

        boolean existsByTrackingCode(String trackingCode);

        @Query("""
                        SELECT f FROM Feedback f
                        WHERE LOWER(f.guestEmail) = LOWER(:email)
                        ORDER BY f.createdAt DESC
                        """)
        List<Feedback> findByGuestEmail(@Param("email") String email);

        @Query("""
                        SELECT f FROM Feedback f
                        WHERE LOWER(f.guestEmail) = LOWER(:email)
                          AND f.isGuest = true
                          AND f.user IS NULL
                        """)
        List<Feedback> findUnclaimedGuestFeedbackByEmail(@Param("email") String email);

        // ==== Tìm kiếm ====
        @Query("""
                        SELECT f FROM Feedback f
                        WHERE LOWER(f.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                           OR LOWER(CAST(f.content AS string)) LIKE LOWER(CONCAT('%', :keyword, '%'))
                        ORDER BY f.createdAt DESC
                        """)
        Page<Feedback> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

        @Query("""
                        SELECT f FROM Feedback f
                        WHERE (:status IS NULL OR f.status = :status)
                          AND (:keyword IS NULL OR :keyword = ''
                               OR LOWER(f.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                               OR LOWER(CAST(f.content AS string)) LIKE LOWER(CONCAT('%', :keyword, '%')))
                        ORDER BY f.createdAt DESC
                        """)
        Page<Feedback> filter(@Param("status") FeedbackStatus status,
                        @Param("keyword") String keyword,
                        Pageable pageable);
}
