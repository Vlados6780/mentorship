package org.example.mentorship.service;

import jakarta.mail.MessagingException;

public interface EmailService {
    void sendVerificationEmail(String to, String subject, String verificationUrl) throws MessagingException;
}
