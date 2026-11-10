package edu.qnu.helpdesk.dto;

import org.springframework.web.multipart.MultipartFile;

import edu.qnu.helpdesk.enums.FeedbackCategory;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackRequest {
    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 200, message = "Tiêu đề tối đa 200 ký tự")
    private String title;

    @NotNull(message = "Vui lòng chọn danh mục")
    private FeedbackCategory category;

    @NotBlank(message = "Nội dung không được để trống")
    @Size(min = 10, max = 5000, message = "Nội dung từ 10 đến 5000 ký tự")
    private String content;

    @Min(value = 1, message = "Mức độ ưu tiên không hợp lệ")
    @Max(value = 3, message = "Mức độ ưu tiên không hợp lệ")
    @Builder.Default
    private Integer priority = 2;

    private MultipartFile attachment;
}
