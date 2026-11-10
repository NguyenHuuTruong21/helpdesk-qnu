package edu.qnu.helpdesk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResetPasswordRequest {

    @NotBlank(message = "Vui lòng nhập MSSV")
    @Pattern(regexp = "^[0-9]{10}$", message = "MSSV phải là 10 chữ số")
    private String studentId;

    @NotBlank(message = "Vui lòng nhập số CCCD/CMND")
    @Pattern(regexp = "^[0-9]{9,12}$", message = "CCCD/CMND phải từ 9 đến 12 chữ số")
    private String citizenId;
}