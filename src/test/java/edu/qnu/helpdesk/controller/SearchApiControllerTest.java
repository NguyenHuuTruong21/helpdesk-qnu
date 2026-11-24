package edu.qnu.helpdesk.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import edu.qnu.helpdesk.entity.Article;
import edu.qnu.helpdesk.service.ArticleService;

@ExtendWith(MockitoExtension.class)
class SearchApiControllerTest {
    @Mock
    private ArticleService articleService;

    @InjectMocks
    private SearchApiController controller;

    @Test
    void searchReturnsFiveSuggestionsForTrimmedKeyword() {
        Article article = Article.builder()
                .title("Campus Wi-Fi")
                .summary("Connect to campus wireless")
                .slug("campus-wifi")
                .build();
        when(articleService.search("wifi", PageRequest.of(0, 5)))
                .thenReturn(new PageImpl<>(List.of(article)));

        var suggestions = controller.search("  wifi  ");

        assertEquals(1, suggestions.size());
        assertEquals("Campus Wi-Fi", suggestions.get(0).title());
        assertEquals("Connect to campus wireless", suggestions.get(0).summary());
        assertEquals("campus-wifi", suggestions.get(0).slug());
        verify(articleService).search("wifi", PageRequest.of(0, 5));
    }

    @Test
    void searchSkipsQueriesShorterThanTwoCharacters() {
        assertEquals(List.of(), controller.search(" a "));

        verifyNoInteractions(articleService);
    }
}
