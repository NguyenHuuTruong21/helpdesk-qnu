package edu.qnu.helpdesk.controller;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.qnu.helpdesk.dto.ArticleSearchSuggestion;
import edu.qnu.helpdesk.service.ArticleService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchApiController {
    private final ArticleService articleService;

    @GetMapping
    public List<ArticleSearchSuggestion> search(@RequestParam(defaultValue = "") String keyword) {
        String query = keyword.trim();
        if (query.length() < 2) {
            return List.of();
        }

        return articleService.search(query, PageRequest.of(0, 5))
                .getContent()
                .stream()
                .map(article -> new ArticleSearchSuggestion(
                        article.getTitle(), article.getSummary(), article.getSlug()))
                .toList();
    }
}
