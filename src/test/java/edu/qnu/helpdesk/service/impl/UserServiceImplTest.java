package edu.qnu.helpdesk.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import edu.qnu.helpdesk.dto.RegisterRequest;
import edu.qnu.helpdesk.entity.Feedback;
import edu.qnu.helpdesk.entity.User;
import edu.qnu.helpdesk.repository.FeedbackRepository;
import edu.qnu.helpdesk.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private FeedbackRepository feedbackRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void registerLinksExistingGuestFeedbackForEmail() {
        RegisterRequest request = RegisterRequest.builder()
                .username("newstudent")
                .password("password")
                .confirmPassword("password")
                .fullName("New Student")
                .citizenId("012345678901")
                .email("  STUDENT@example.com ")
                .build();
        Feedback guestFeedback = Feedback.builder()
                .guestEmail("student@example.com")
                .isGuest(true)
                .build();

        when(userRepository.existsByUsername("newstudent")).thenReturn(false);
        when(userRepository.existsByEmail("student@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(feedbackRepository.findUnclaimedGuestFeedbackByEmail("student@example.com"))
                .thenReturn(List.of(guestFeedback));

        User registeredUser = userService.register(request);

        assertEquals("student@example.com", registeredUser.getEmail());
        assertEquals("012345678901", registeredUser.getCitizenId());
        assertSame(registeredUser, guestFeedback.getUser());
        assertFalse(guestFeedback.getIsGuest());
        verify(feedbackRepository).saveAll(List.of(guestFeedback));
    }

    @Test
    void registerAllowsMissingCitizenId() {
        RegisterRequest request = RegisterRequest.builder()
                .username("newstudent")
                .password("password")
                .confirmPassword("password")
                .fullName("New Student")
                .citizenId("")
                .build();

        when(userRepository.existsByUsername("newstudent")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User registeredUser = userService.register(request);

        assertNull(registeredUser.getCitizenId());
        assertEquals("encoded-password", registeredUser.getPassword());
    }

    @Test
    void loginLinksExistingGuestFeedbackForAccountEmail() {
        User existingUser = User.builder()
                .username("existingstudent")
                .email("  STUDENT@example.com ")
                .build();
        Feedback guestFeedback = Feedback.builder()
                .guestEmail("student@example.com")
                .isGuest(true)
                .build();

        when(userRepository.findByUsername("existingstudent")).thenReturn(Optional.of(existingUser));
        when(feedbackRepository.findUnclaimedGuestFeedbackByEmail("student@example.com"))
                .thenReturn(List.of(guestFeedback));

        userService.claimGuestFeedbackForUser("existingstudent");

        assertSame(existingUser, guestFeedback.getUser());
        assertFalse(guestFeedback.getIsGuest());
        verify(feedbackRepository).saveAll(List.of(guestFeedback));
    }

    @Test
    void loginSkipsGuestFeedbackLookupWhenAccountHasNoEmail() {
        User existingUser = User.builder()
                .username("existingstudent")
                .build();
        when(userRepository.findByUsername("existingstudent")).thenReturn(Optional.of(existingUser));

        userService.claimGuestFeedbackForUser("existingstudent");

        verify(feedbackRepository, never()).findUnclaimedGuestFeedbackByEmail(any());
    }
}
