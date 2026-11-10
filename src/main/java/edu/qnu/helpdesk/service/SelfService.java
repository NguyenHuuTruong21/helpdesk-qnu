package edu.qnu.helpdesk.service;

import java.util.Map;

import edu.qnu.helpdesk.dto.CreateEmailRequest;
import edu.qnu.helpdesk.dto.UpdateInfoRequest;

public interface SelfService {
    // Tra cứu thông tin
    Map<String, Object> lookupAccount(String mssv);
    
    // Tự động cấp lại mật khẩu
    String resetPassword(String mssv, String citizenId);
    
    void requestCreateEmail(CreateEmailRequest request);
    void updateContactInfo(UpdateInfoRequest request);
}
