package edu.qnu.helpdesk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class CreateEmailRequest {

    @NotBlank(message = "Vui lòng nhập MSSV")
    @Pattern(regexp = "^[0-9]{10}$", message = "MSSV phải là 10 chữ số")
    private String studentId;

    @NotBlank(message = "Vui lòng nhập họ và tên")
    private String fullName;

    @NotBlank(message = "Vui lòng nhập số điện thoại")
    @Pattern(regexp = "^(0|\\+84)[0-9]{9,10}$", message = "Số điện thoại không hợp lệ")
    private String phone;

    @NotBlank(message = "Vui lòng nhập Email cá nhân")
    @jakarta.validation.constraints.Email(message = "Email cá nhân không hợp lệ")
    private String personalEmail;
}