package edu.qnu.helpdesk.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import edu.qnu.helpdesk.dto.RegisterRequest;
import edu.qnu.helpdesk.entity.User;
import edu.qnu.helpdesk.repository.UserRepository.UserSummary;

public interface UserService {
    User register(RegisterRequest request);

    void claimGuestFeedbackForUser(String username);

    Optional<User> findByUsername(String username);

    User findById(Long id);

    User getCurrentUser();

    List<User> findAllAdmins();

    Page<UserSummary> findUsers(Pageable pageable);

    long countUsers();

    long countAdmins();
}
