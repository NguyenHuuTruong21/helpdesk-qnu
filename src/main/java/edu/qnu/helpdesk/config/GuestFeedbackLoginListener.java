package edu.qnu.helpdesk.config;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import edu.qnu.helpdesk.service.UserService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GuestFeedbackLoginListener {
    private final UserService userService;

    @EventListener
    public void onLoginSuccess(InteractiveAuthenticationSuccessEvent event) {
        userService.claimGuestFeedbackForUser(event.getAuthentication().getName());
    }
}
