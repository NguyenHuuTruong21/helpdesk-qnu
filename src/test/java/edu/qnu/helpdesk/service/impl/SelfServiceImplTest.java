package edu.qnu.helpdesk.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import edu.qnu.helpdesk.dto.UpdateInfoRequest;
import edu.qnu.helpdesk.entity.Feedback;
import edu.qnu.helpdesk.entity.User;
import edu.qnu.helpdesk.repository.FeedbackRepository;
import edu.qnu.helpdesk.repository.UserRepository;
import edu.qnu.helpdesk.service.EmailService;

@ExtendWith(MockitoExtension.class)
class SelfServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private FeedbackRepository feedbackRepository;

    @InjectMocks
    private SelfServiceImpl selfService;

    @Test
    void updateContactInfoKeepsNewCitizenIdPendingForAdminApproval() {
        User user = User.builder()
                .username("1234567890")
                .citizenId("012345678901")
                .build();
        UpdateInfoRequest request = UpdateInfoRequest.builder()
                .studentId("1234567890")
                .citizenId("012345678901")
                .newCitizenId("123456789012")
                .build();
        when(userRepository.findByUsername("1234567890")).thenReturn(Optional.of(user));

        selfService.updateContactInfo(request);

        ArgumentCaptor<Feedback> feedbackCaptor = ArgumentCaptor.forClass(Feedback.class);
        verify(feedbackRepository).save(feedbackCaptor.capture());
        assertEquals("123456789012", feedbackCaptor.getValue().getRequestedCitizenId());
        assertFalse(feedbackCaptor.getValue().isCitizenIdUpdateApproved());
        assertEquals("012345678901", user.getCitizenId());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateContactInfoAllowsAddingMissingCitizenIdForAdminApproval() {
        User user = User.builder()
                .username("1234567890")
                .build();
        UpdateInfoRequest request = UpdateInfoRequest.builder()
                .studentId("1234567890")
                .newCitizenId("123456789012")
                .build();
        when(userRepository.findByUsername("1234567890")).thenReturn(Optional.of(user));

        selfService.updateContactInfo(request);

        ArgumentCaptor<Feedback> feedbackCaptor = ArgumentCaptor.forClass(Feedback.class);
        verify(feedbackRepository).save(feedbackCaptor.capture());
        assertEquals("123456789012", feedbackCaptor.getValue().getRequestedCitizenId());
        assertNull(user.getCitizenId());
        verify(userRepository, never()).save(any(User.class));
    }
}
