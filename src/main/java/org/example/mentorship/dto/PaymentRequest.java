package org.example.mentorship.dto;

public record PaymentRequest(
        String cardNumber,
        String expiryDate,
        String cardholderName,
        String cvv
) {}
