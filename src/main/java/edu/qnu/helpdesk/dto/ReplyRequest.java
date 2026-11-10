package edu.qnu.helpdesk.dto;

import edu.qnu.helpdesk.enums.FeedbackStatus;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReplyRequest {
    @NotBlank(message = "Nội dung trả lời không được để trống")
    @Size(min = 5, max = 5000, message = "Nội dung từ 5 đến 5000 ký tự")
    private String content;

    @NotNull(message = "Vui lòng chọn trạng thái")
    private FeedbackStatus status;
}
