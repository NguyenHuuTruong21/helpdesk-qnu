package edu.qnu.helpdesk.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrackTicketRequest {
    @NotBlank(message = "Vui lòng nhập mã tra cứu")
    private String trackingCode;

    @NotBlank(message = "Vui lòng nhập email")
    @Email(message = "Email không hợp lệ")
    private String email;
}
