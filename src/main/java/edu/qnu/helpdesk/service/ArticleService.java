package edu.qnu.helpdesk.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import edu.qnu.helpdesk.entity.Article;
import edu.qnu.helpdesk.entity.User;
import edu.qnu.helpdesk.enums.FeedbackCategory;

public interface ArticleService {
    Article createArticle(Article article, User author);

    Article updateArticle(Long id, Article updated);

    void deleteArticle(Long id);

    Article findById(Long id);

    Article findBySlug(String slug);

    Page<Article> findAllPublished(Pageable pageable);

    Page<Article> findByCategory(FeedbackCategory category, Pageable pageable);

    Page<Article> search(String keyword, Pageable pageable);

    List<Article> getTopFeatured();

    List<Article> getTopViewed();

    void incrementView(Long id);

    void markHelpful(Long id);

    void markNotHelpful(Long id);

    long countAll();

    String generateUniqueSlug(String title, Long excludeId);
}
