package org.example.mentorship.service;

import org.example.mentorship.dto.PaymentRequest;
import org.example.mentorship.dto.PaymentResponse;
import org.springframework.http.ResponseEntity;

public interface SubscriptionService {
    ResponseEntity<PaymentResponse> processPayment(PaymentRequest paymentRequest);
}
