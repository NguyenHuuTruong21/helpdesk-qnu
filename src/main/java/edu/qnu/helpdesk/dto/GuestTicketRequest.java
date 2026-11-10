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
public class GuestTicketRequest {
    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 100, message = "Họ tên tối đa 100 ký tự")
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @Size(max = 100)
    private String email;

    @Pattern(regexp = "^(0|\\+84)[0-9]{9,10}$", message = "Số điện thoại không hợp lệ")
    private String phone;

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 200)
    private String title;

    @NotNull(message = "Vui lòng chọn danh mục")
    private FeedbackCategory category;

    @NotBlank(message = "Nội dung không được để trống")
    @Size(min = 10, max = 5000)
    private String content;

    @Builder.Default
    private Integer priority = 2;

    private MultipartFile attachment;
}
