package org.example.mentorship.service;

import org.example.mentorship.entity.User;

public interface EmailVerificationService {
    void createVerificationToken(User user);
    boolean confirmEmail(String token);
    void resendVerificationToken(String email);
}
