package edu.qnu.helpdesk.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import edu.qnu.helpdesk.entity.Article;
import edu.qnu.helpdesk.enums.FeedbackCategory;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    Optional<Article> findBySlug(String slug);

    boolean existsBySlug(String slug);

    Page<Article> findByIsPublishedTrueOrderByCreatedAtDesc(Pageable pageable);

    Page<Article> findByIsPublishedTrueAndCategoryOrderByCreatedAtDesc(
            FeedbackCategory category, Pageable pageable);

    List<Article> findTop5ByIsFeaturedTrueAndIsPublishedTrueOrderByViewCountDesc();

    List<Article> findTop5ByIsPublishedTrueOrderByViewCountDesc();

    @Query(value = """
            SELECT * FROM articles a
            WHERE a.is_published = 1
              AND (a.title   LIKE CONCAT('%', :kw, '%')
                OR a.summary LIKE CONCAT('%', :kw, '%')
                OR a.content LIKE CONCAT('%', :kw, '%'))
            ORDER BY a.view_count DESC
            """, countQuery = """
            SELECT COUNT(*) FROM articles a
            WHERE a.is_published = 1
              AND (a.title   LIKE CONCAT('%', :kw, '%')
                OR a.summary LIKE CONCAT('%', :kw, '%')
                OR a.content LIKE CONCAT('%', :kw, '%'))
            """, nativeQuery = true)
    Page<Article> searchPublished(@Param("kw") String keyword, Pageable pageable);

    @Modifying
    @Query("UPDATE Article a SET a.viewCount = a.viewCount + 1 WHERE a.id = :id")
    void incrementViewCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Article a SET a.helpfulCount = a.helpfulCount + 1 WHERE a.id = :id")
    void incrementHelpful(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Article a SET a.notHelpfulCount = a.notHelpfulCount + 1 WHERE a.id = :id")
    void incrementNotHelpful(@Param("id") Long id);
}
