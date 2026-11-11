package edu.qnu.helpdesk.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import edu.qnu.helpdesk.entity.Feedback;
import edu.qnu.helpdesk.entity.User;
import edu.qnu.helpdesk.repository.FeedbackRepository;
import edu.qnu.helpdesk.repository.UserRepository;
import edu.qnu.helpdesk.service.FileStorageService;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceImplTest {
    @Mock
    private FeedbackRepository feedbackRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FeedbackServiceImpl feedbackService;

    @Test
    void approveCitizenIdUpdateSynchronizesUserProfile() {
        User user = User.builder()
                .username("1234567890")
                .citizenId("012345678901")
                .build();
        Feedback feedback = Feedback.builder()
                .user(user)
                .requestedCitizenId("123456789012")
                .build();
        when(feedbackRepository.findById(42L)).thenReturn(Optional.of(feedback));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(feedbackRepository.save(any(Feedback.class))).thenAnswer(invocation -> invocation.getArgument(0));

        feedbackService.approveCitizenIdUpdate(42L);

        assertEquals("123456789012", user.getCitizenId());
        assertTrue(feedback.isCitizenIdUpdateApproved());
        verify(userRepository).save(user);
        verify(feedbackRepository).save(feedback);
    }
}
