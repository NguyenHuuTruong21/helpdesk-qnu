package edu.qnu.helpdesk.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import edu.qnu.helpdesk.dto.UpdateInfoRequest;
import edu.qnu.helpdesk.entity.Feedback;
import edu.qnu.helpdesk.entity.User;
import edu.qnu.helpdesk.exception.BadRequestException;
import edu.qnu.helpdesk.repository.FeedbackRepository;
import edu.qnu.helpdesk.repository.UserRepository;
import edu.qnu.helpdesk.service.EmailService;

@ExtendWith(MockitoExtension.class)
class SelfServiceImplTest {
    @Mock
    private UserRepository userRepository;

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
                .password("existing-login-hash")
                .citizenId("012345678901")
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
        assertFalse(feedbackCaptor.getValue().isCitizenIdUpdateApproved());
        assertEquals("012345678901", user.getCitizenId());
        assertEquals("existing-login-hash", user.getPassword());
        verify(userRepository, org.mockito.Mockito.never()).save(any(User.class));
    }

    @Test
    void updateContactInfoAllowsAddingMissingCitizenIdForAdminApproval() {
        User user = User.builder()
                .username("1234567890")
                .password("existing-login-hash")
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
        assertEquals("existing-login-hash", user.getPassword());
    }

    @Test
    void updateContactInfoDoesNotRequireCitizenIdForContactChanges() {
        User user = User.builder()
                .username("1234567890")
                .password("existing-login-hash")
                .citizenId("012345678901")
                .build();
        UpdateInfoRequest request = UpdateInfoRequest.builder()
                .studentId("1234567890")
                .citizenId("012345678901")
                .newPhone("0123456789")
                .build();
        when(userRepository.findByUsername("1234567890")).thenReturn(Optional.of(user));

        selfService.updateContactInfo(request);

        assertEquals("0123456789", user.getPhone());
        assertEquals("existing-login-hash", user.getPassword());
        verify(userRepository).save(user);
        verify(feedbackRepository).save(any(Feedback.class));
    }

    @Test
    void updateContactInfoRequiresVerificationWhenProfileHasNoCitizenId() {
        User user = User.builder()
                .username("1234567890")
                .phone("0987654321")
                .build();
        UpdateInfoRequest request = UpdateInfoRequest.builder()
                .studentId("1234567890")
                .newPhone("0123456789")
                .build();
        when(userRepository.findByUsername("1234567890")).thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class, () -> selfService.updateContactInfo(request));

        assertEquals("0987654321", user.getPhone());
        verify(feedbackRepository, never()).save(any(Feedback.class));
    }

    @Test
    void lookupAccountOnlyReportsWhetherCitizenIdExists() {
        User user = User.builder()
                .username("1234567890")
                .citizenId("012345678901")
                .build();
        when(userRepository.findByUsername("1234567890")).thenReturn(Optional.of(user));

        Map<String, Object> result = selfService.lookupAccount("1234567890");

        assertTrue((Boolean) result.get("hasCitizenId"));
        assertFalse(result.containsKey("citizenId"));
    }

    @Test
    void resetPasswordDoesNotChangeHelpDeskLoginPassword() {
        User user = User.builder()
                .username("1234567890")
                .password("existing-login-hash")
                .citizenId("012345678901")
                .personalEmail("student@example.com")
                .build();
        when(userRepository.findByUsername("1234567890")).thenReturn(Optional.of(user));

        String sentEmail = selfService.resetPassword("1234567890", "012345678901");

        assertEquals("student@example.com", sentEmail);
        assertEquals("existing-login-hash", user.getPassword());
        verify(emailService).sendNewPasswordEmail(
                eq("student@example.com"),
                eq(null),
                eq("1234567890"),
                anyString());
        verify(userRepository, never()).save(any(User.class));
    }
}
