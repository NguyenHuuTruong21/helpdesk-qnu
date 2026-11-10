package edu.qnu.helpdesk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor
public class AccountLookupRequest {
    @NotBlank(message = "Vui lòng nhập MSSV")
    @Pattern(regexp = "^[0-9]{10}$", message = "MSSV phải là 10 chữ số")
    private String studentId;
}
