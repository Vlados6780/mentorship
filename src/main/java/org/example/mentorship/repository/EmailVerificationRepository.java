package org.example.mentorship.repository;

import org.example.mentorship.entity.EmailVerification;
import org.example.mentorship.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Integer> {
    Optional<EmailVerification> findByToken(String token);
    Optional<EmailVerification> findByUser(User user);
}
