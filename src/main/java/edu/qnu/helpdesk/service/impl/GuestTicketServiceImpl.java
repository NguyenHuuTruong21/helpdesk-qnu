package edu.qnu.helpdesk.service.impl;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.qnu.helpdesk.dto.GuestTicketRequest;
import edu.qnu.helpdesk.entity.Feedback;
import edu.qnu.helpdesk.enums.FeedbackStatus;
import edu.qnu.helpdesk.exception.BadRequestException;
import edu.qnu.helpdesk.exception.ResourceNotFoundException;
import edu.qnu.helpdesk.repository.FeedbackRepository;
import edu.qnu.helpdesk.service.FileStorageService;
import edu.qnu.helpdesk.service.GuestTicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuestTicketServiceImpl implements GuestTicketService {
    private final FeedbackRepository feedbackRepository;
    private final FileStorageService fileStorageService;

    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    @Transactional
    public Feedback createGuestTicket(GuestTicketRequest request) {
        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new BadRequestException("Nội dung không được để trống");
        }

        // Sinh mã tracking
        String trackingCode = generateTrackingCode();

        // Xử lý file đính kèm
        String storedFileName = null;
        String originalFileName = null;
        if (request.getAttachment() != null && !request.getAttachment().isEmpty()) {
            storedFileName = fileStorageService.storeFile(request.getAttachment());
            originalFileName = request.getAttachment().getOriginalFilename();
        }

        Feedback feedback = Feedback.builder()
                .trackingCode(trackingCode)
                .user(null)
                .isGuest(true)
                .guestName(request.getFullName())
                .guestEmail(request.getEmail().trim().toLowerCase(Locale.ROOT))
                .guestPhone(request.getPhone())
                .title(request.getTitle())
                .category(request.getCategory())
                .content(request.getContent())
                .priority(request.getPriority() != null ? request.getPriority() : 2)
                .attachment(storedFileName)
                .attachmentName(originalFileName)
                .status(FeedbackStatus.PENDING)
                .build();

        Feedback saved = feedbackRepository.save(feedback);
        log.info("Guest [{}] created ticket: {} - category: {}",
                request.getEmail(), trackingCode, request.getCategory());
        return saved;
    }

    @Override
    public Feedback findByTrackingCodeAndEmail(String trackingCode, String email) {
        if (trackingCode == null || email == null) {
            throw new BadRequestException("Vui lòng nhập đầy đủ mã tra cứu và email");
        }

        Feedback feedback = feedbackRepository
                .findByTrackingCodeAndGuestEmail(trackingCode.trim(), email.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy yêu cầu. Vui lòng kiểm tra lại mã tra cứu và email."));

        return feedback;
    }

    @Override
    public List<Feedback> findByEmail(String email) {
        if (email == null || email.isBlank())
            return List.of();
        return feedbackRepository.findByGuestEmail(email.trim().toLowerCase());
    }

    @Override
    public String generateTrackingCode() {
        String code;
        int attempts = 0;
        do {
            code = generateCode();
            attempts++;
            if (attempts > 10) {
                throw new RuntimeException("Không thể sinh mã tra cứu, vui lòng thử lại");
            }
        } while (feedbackRepository.existsByTrackingCode(code));
        return code;
    }

    private String generateCode() {
        // Format: QNU-YYYYMMDD-XXXXXX (VD: QNU-20250115-A7K9M2)
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append(CODE_CHARS.charAt(RANDOM.nextInt(CODE_CHARS.length())));
        }
        return "QNU-" + date + "-" + sb;
    }
}