package org.example.mentorship.controller;

import org.example.mentorship.dto.PaymentRequest;
import org.example.mentorship.dto.PaymentResponse;
import org.example.mentorship.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/subscription")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final AsyncTaskExecutor executor;

    @Autowired
    public SubscriptionController(
            SubscriptionService subscriptionService,
            @Qualifier("securityAwareAsyncExecutor") AsyncTaskExecutor executor) {
        this.subscriptionService = subscriptionService;
        this.executor = executor;
    }

    @PostMapping("/pay")
    public CompletableFuture<ResponseEntity<PaymentResponse>> processPayment(
            @RequestBody PaymentRequest paymentRequest) {
        return CompletableFuture.supplyAsync(
                () -> subscriptionService.processPayment(paymentRequest),
                executor);
    }

}
