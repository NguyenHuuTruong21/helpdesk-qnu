package edu.qnu.helpdesk.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.qnu.helpdesk.entity.User;
import edu.qnu.helpdesk.enums.Role;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByUsernameOrEmail(String username, String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<User> findByRole(Role role);

    Page<UserSummary> findByRole(Role role, Pageable pageable);

    long countByRole(Role role);

    interface UserSummary {
        Long getId();

        String getUsername();

        String getFullName();

        String getEmail();

        String getPhone();

        Boolean getEnabled();

        LocalDateTime getCreatedAt();
    }
}
