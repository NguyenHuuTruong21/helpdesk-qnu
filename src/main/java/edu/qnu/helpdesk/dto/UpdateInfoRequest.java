package edu.qnu.helpdesk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class UpdateInfoRequest {

    @NotBlank(message = "Vui lòng nhập MSSV")
    @Pattern(regexp = "^[0-9]{10}$", message = "MSSV phải là 10 chữ số")
    private String studentId;

    @Pattern(regexp = "^(?:[0-9]{9,12})?$", message = "CCCD/CMND phải từ 9 đến 12 chữ số")
    private String citizenId;

    @Pattern(regexp = "^(?:[0-9]{9,12})?$", message = "CCCD/CMND mới phải từ 9 đến 12 chữ số")
    private String newCitizenId;

    @Pattern(regexp = "^(0|\\+84)[0-9]{9,10}$", message = "Số điện thoại không hợp lệ")
    private String newPhone;

    @jakarta.validation.constraints.Email(message = "Email cá nhân không hợp lệ")
    private String newPersonalEmail;
}