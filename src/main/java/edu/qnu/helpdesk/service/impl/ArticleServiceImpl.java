package edu.qnu.helpdesk.service.impl;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.qnu.helpdesk.entity.Article;
import edu.qnu.helpdesk.entity.User;
import edu.qnu.helpdesk.enums.FeedbackCategory;
import edu.qnu.helpdesk.exception.ResourceNotFoundException;
import edu.qnu.helpdesk.repository.ArticleRepository;
import edu.qnu.helpdesk.service.ArticleService;
import edu.qnu.helpdesk.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {
    private final ArticleRepository articleRepository;
    private final FileStorageService fileStorageService;

    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]+");

    @Override
    @Transactional
    public Article createArticle(Article article, User author) {
        article.setSlug(generateUniqueSlug(article.getTitle(), null));
        article.setAuthor(author);
        if (Boolean.TRUE.equals(article.getIsPublished())) {
            article.setPublishedAt(LocalDateTime.now());
        }
        Article saved = articleRepository.save(article);
        log.info("Article created: {} by {}", saved.getSlug(), author.getUsername());
        return saved;
    }

    public Article updateArticle(Long id, Article updated) {
        Article article = findById(id);
        article.setTitle(updated.getTitle());
        article.setSummary(updated.getSummary());
        article.setContent(updated.getContent());
        article.setCategory(updated.getCategory());
        article.setIsPublished(updated.getIsPublished());
        article.setIsFeatured(updated.getIsFeatured());

        // Nếu đổi title → cập nhật slug
        if (!article.getTitle().equals(updated.getTitle())) {
            article.setSlug(generateUniqueSlug(updated.getTitle(), id));
        }

        // Nếu mới publish lần đầu
        if (Boolean.TRUE.equals(updated.getIsPublished()) && article.getPublishedAt() == null) {
            article.setPublishedAt(LocalDateTime.now());
        }

        // Xử lý file đính kèm
        if (updated.getAttachment() != null) {
            if (article.getAttachment() != null) {
                fileStorageService.deleteFile(article.getAttachment());
            }
            article.setAttachment(updated.getAttachment());
            article.setAttachmentName(updated.getAttachmentName());
        }

        return articleRepository.save(article);
    }

    @Override
    @Transactional
    public void deleteArticle(Long id) {
        Article article = findById(id);
        if (article.getAttachment() != null) {
            fileStorageService.deleteFile(article.getAttachment());
        }
        articleRepository.delete(article);
        log.info("Article deleted: {}", id);
    }

    @Override
    public Article findById(Long id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bài viết", id));
    }

    @Override
    public Article findBySlug(String slug) {
        return articleRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài viết: " + slug));
    }

    @Override
    public Page<Article> findAllPublished(Pageable pageable) {
        return articleRepository.findByIsPublishedTrueOrderByCreatedAtDesc(pageable);
    }

    @Override
    public Page<Article> findByCategory(FeedbackCategory category, Pageable pageable) {
        return articleRepository.findByIsPublishedTrueAndCategoryOrderByCreatedAtDesc(category, pageable);
    }

    @Override
    public Page<Article> search(String keyword, Pageable pageable) {
        String kw = (keyword == null) ? "" : keyword.trim();
        return articleRepository.searchPublished(kw, pageable);
    }

    @Override
    public List<Article> getTopFeatured() {
        return articleRepository.findTop5ByIsFeaturedTrueAndIsPublishedTrueOrderByViewCountDesc();
    }

    @Override
    public List<Article> getTopViewed() {
        return articleRepository.findTop5ByIsPublishedTrueOrderByViewCountDesc();
    }

    @Override
    @Transactional
    public void incrementView(Long id) {
        articleRepository.incrementViewCount(id);
    }

    @Override
    @Transactional
    public void markHelpful(Long id) {
        articleRepository.incrementHelpful(id);
    }

    @Override
    @Transactional
    public void markNotHelpful(Long id) {
        articleRepository.incrementNotHelpful(id);
    }

    @Override
    public long countAll() {
        return articleRepository.count();
    }

    @Override
    public String generateUniqueSlug(String title, Long excludeId) {
        String slug = toSlug(title);
        String baseSlug = slug;
        int counter = 1;
        while (articleRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter++;
        }
        return slug;
    }

    private String toSlug(String input) {
        String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = NON_LATIN.matcher(normalized).replaceAll("");
        return slug.toLowerCase();
    }
}
