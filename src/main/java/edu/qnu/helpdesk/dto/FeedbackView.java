package edu.qnu.helpdesk.dto;

import java.time.LocalDateTime;

import edu.qnu.helpdesk.enums.FeedbackCategory;
import edu.qnu.helpdesk.enums.FeedbackStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackView {
    private Long id;
    private String title;
    private FeedbackCategory category;
    private String content;
    private FeedbackStatus status;
    private Integer priority;
    private String attachment;
    private String attachmentName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Người gửi
    private Long userId;
    private String username;
    private String fullName;

    // Số reply + reply mới nhất
    private long replyCount;
    private LocalDateTime lastReplyAt;
}
