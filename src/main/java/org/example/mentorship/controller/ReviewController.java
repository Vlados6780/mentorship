package org.example.mentorship.controller;

import org.example.mentorship.dto.ReviewRequest;
import org.example.mentorship.dto.ReviewUpdateRequest;
import org.example.mentorship.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final AsyncTaskExecutor executor;
    private final ReviewService reviewService;

    @Autowired
    public ReviewController(
            @Qualifier("securityAwareAsyncExecutor") AsyncTaskExecutor executor,
            ReviewService reviewService) {
        this.executor = executor;
        this.reviewService = reviewService;
    }

    @PostMapping
    public CompletableFuture<ResponseEntity<?>> createReview(@RequestBody ReviewRequest reviewRequest) {
        return CompletableFuture.supplyAsync(() -> reviewService.createReview(reviewRequest), executor);
    }

    @GetMapping("/mentor/{mentorId}")
    public CompletableFuture<ResponseEntity<?>> getMentorReviews(@PathVariable Integer mentorId) {
        return CompletableFuture.supplyAsync(() -> reviewService.getMentorReviews(mentorId), executor);
    }

    @PutMapping("/{reviewId}")
    public CompletableFuture<ResponseEntity<?>> updateReview(
            @PathVariable Integer reviewId,
            @RequestBody ReviewUpdateRequest updateRequest) {
        return CompletableFuture.supplyAsync(
                () -> reviewService.updateReview(reviewId, updateRequest),
                executor);
    }

    @DeleteMapping("/{reviewId}")
    public CompletableFuture<ResponseEntity<?>> deleteReview(@PathVariable Integer reviewId) {
        return CompletableFuture.supplyAsync(
                () -> reviewService.deleteReview(reviewId),
                executor);
    }
}
