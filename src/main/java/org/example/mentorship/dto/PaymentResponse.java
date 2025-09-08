package org.example.mentorship.dto;

public record PaymentResponse(
        boolean success,
        String message,
        String transactionId
) {}
