package org.example.mentorship.service;

import jakarta.mail.MessagingException;
import org.example.mentorship.entity.EmailVerification;
import org.example.mentorship.entity.User;
import org.example.mentorship.repository.EmailVerificationRepository;
import org.example.mentorship.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class DefaultEmailVerificationService implements EmailVerificationService {
    private final EmailVerificationRepository emailVerificationRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Value("${app.verification.expiration-hours}")
    private int expirationHours;

    @Autowired
    public DefaultEmailVerificationService(
            EmailVerificationRepository emailVerificationRepository,
            UserRepository userRepository,
            EmailService emailService) {
        this.emailVerificationRepository = emailVerificationRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public void createVerificationToken(User user) {
        // Generate a new token
        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(expirationHours);

        // Find existing record and update it, or create a new one
        EmailVerification verification = emailVerificationRepository.findByUser(user)
                .orElse(new EmailVerification());

        verification.setUser(user);
        verification.setToken(token);
        verification.setExpiresAt(expiryDate);

        emailVerificationRepository.save(verification);

        String verificationUrl = "http://localhost:4200/verify-email?token=" + token;

        // Send email
        try {
            emailService.sendVerificationEmail(
                    user.getEmail(),
                    "Registration Confirmation",
                    verificationUrl
            );
        } catch (MessagingException e) {
            throw new RuntimeException("Error sending email: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean confirmEmail(String token) {
        EmailVerification verification = emailVerificationRepository.findByToken(token)
                .orElse(null);

        if (verification == null) {
            return false;
        }

        // Check if the token has expired
        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            emailVerificationRepository.delete(verification);
            return false;
        }

        // Confirm email
        User user = verification.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        // Delete used token
        emailVerificationRepository.delete(verification);

        return true;
    }

    @Override
    @Transactional
    public void resendVerificationToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User with email " + email + " not found"));

        if (user.isEmailVerified()) {
            throw new RuntimeException("Email already verified");
        }

        createVerificationToken(user);
    }
}
