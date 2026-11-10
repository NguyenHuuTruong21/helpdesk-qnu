package edu.qnu.helpdesk.service;

import java.util.List;

import edu.qnu.helpdesk.dto.GuestTicketRequest;
import edu.qnu.helpdesk.entity.Feedback;

public interface GuestTicketService {
    /* Tạo tícker không cần đăng nhập */
    Feedback createGuestTicket(GuestTicketRequest request);

    /* Tra cứu ticker theo mã + email */
    Feedback findByTrackingCodeAndEmail(String trackingCode, String email);

    /* Lây tất cả các ticker 1 email */
    List<Feedback> findByEmail(String email);

    /* Sinh mã tracking code unique */
    String generateTrackingCode();
}
