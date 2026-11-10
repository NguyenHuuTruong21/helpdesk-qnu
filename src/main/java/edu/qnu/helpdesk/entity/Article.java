package edu.qnu.helpdesk.entity;

import edu.qnu.helpdesk.enums.FeedbackCategory;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "articles", indexes = {
        @Index(name = "idx_article_slug", columnList = "slug", unique = true),
        @Index(name = "idx_article_category", columnList = "category"),
        @Index(name = "idx_article_published", columnList = "is_published")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 250)
    private String slug;

    @Column(nullable = false, length = 250)
    private String title;

    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String content;

    @Column(length = 500)
    private String summary;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private FeedbackCategory category;

    @Column(name = "view_count")
    @Builder.Default
    private Long viewCount = 0L;

    @Column(name = "helpful_count")
    @Builder.Default
    private Long helpfulCount = 0L;

    @Column(name = "not_helpful_count")
    @Builder.Default
    private Long notHelpfulCount = 0L;

    @Column(name = "is_published", nullable = false)
    @Builder.Default
    private Boolean isPublished = true;

    @Column(name = "is_featured", nullable = false)
    @Builder.Default
    private Boolean isFeatured = false;

    @Column(name = "attachment", length = 255)
    private String attachment;

    @Column(name = "attachment_name", length = 255)
    private String attachmentName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", foreignKey = @ForeignKey(name = "fk_article_author"))
    private User author;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
