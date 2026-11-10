package edu.qnu.helpdesk.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import edu.qnu.helpdesk.entity.Article;
import edu.qnu.helpdesk.enums.FeedbackCategory;
import edu.qnu.helpdesk.service.ArticleService;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/knowledge-base")
@RequiredArgsConstructor
public class KnowledgeBaseController {
    private final ArticleService articleService;

    @GetMapping
    public String list(@RequestParam(required = false) FeedbackCategory category,
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Article> articles = (keyword.isBlank() && category == null)
                ? articleService.findAllPublished(pageable)
                : (keyword.isBlank()
                        ? articleService.findByCategory(category, pageable)
                        : articleService.search(keyword, pageable));

        model.addAttribute("articles", articles);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", articles.getTotalPages());
        model.addAttribute("categories", FeedbackCategory.values());
        model.addAttribute("selectedCategory", category);
        model.addAttribute("keyword", keyword);
        model.addAttribute("topArticles", articleService.getTopViewed());

        return "public/knowledge-base";
    }

    @GetMapping("/{slug}")
    public String detail(@PathVariable String slug, Model model) {
        Article article = articleService.findBySlug(slug);
        articleService.incrementView(article.getId());

        model.addAttribute("article", article);
        model.addAttribute("relatedArticles",
                articleService.getTopViewed().stream()
                        .filter(a -> !a.getId().equals(article.getId()))
                        .limit(5).toList());
        return "public/article-detail";
    }

    @PostMapping("/{id}/helpful")
    public String markHelpful(@PathVariable Long id) {
        articleService.markHelpful(id);
        return "redirect:/knowledge-base";
    }

    @PostMapping("/{id}/not-helpful")
    public String markNotHelpful(@PathVariable Long id) {
        articleService.markNotHelpful(id);
        return "redirect:/knowledge-base";
    }
}
