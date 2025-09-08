package org.example.mentorship.controller;

import org.example.mentorship.service.EmailVerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/verification")
public class VerificationController {

    private final EmailVerificationService verificationService;

    @Autowired
    public VerificationController(EmailVerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @GetMapping("/confirm")
    public CompletableFuture<ResponseEntity<?>> confirmEmail(@RequestParam("token") String token) {
        return CompletableFuture.supplyAsync(() -> {
            boolean isConfirmed = verificationService.confirmEmail(token);
            if (isConfirmed) {
                return ResponseEntity.ok(Map.of("message", "Email successfully confirmed"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Invalid or expired token"));
            }
        });
    }

    @PostMapping("/resend")
    public CompletableFuture<ResponseEntity<?>> resendVerificationEmail(@RequestParam("email") String email) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                verificationService.resendVerificationToken(email);
                return ResponseEntity.ok(Map.of("message", "Confirmation email resent"));
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
            }
        });
    }
}
