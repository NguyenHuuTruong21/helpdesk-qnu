package edu.qnu.helpdesk.service;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import edu.qnu.helpdesk.entity.User;
import edu.qnu.helpdesk.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy user: " + username));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(getAuthorities(user))
                .disabled(!Boolean.TRUE.equals(user.getEnabled()))
                .build();
    }

    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        String role = user.getRole() != null ? user.getRole().name() : "USER";
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }
}