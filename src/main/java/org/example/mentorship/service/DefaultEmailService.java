package org.example.mentorship.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class DefaultEmailService implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Autowired
    public DefaultEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(String to, String subject, String verificationUrl) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);

        String htmlContent = "<html><body>"
                + "<h2>Registration Confirmation</h2>"
                + "<p>Please follow the link below to confirm your email:</p>"
                + "<a href='" + verificationUrl + "'>Confirm email</a>"
                + "<p>The link is valid for 24 hours.</p>"
                + "</body></html>";

        helper.setText(htmlContent, true);
        mailSender.send(message);
    }
}
