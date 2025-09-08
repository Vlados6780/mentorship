package org.example.mentorship.service;

import org.example.mentorship.dto.ReviewRequest;
import org.example.mentorship.dto.ReviewResponse;
import org.example.mentorship.dto.ReviewUpdateRequest;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ReviewService {
    ResponseEntity<?> createReview(ReviewRequest reviewRequest);
    ResponseEntity<List<ReviewResponse>> getMentorReviews(Integer mentorId);
    ResponseEntity<?> updateReview(Integer reviewId, ReviewUpdateRequest reviewRequest);
    ResponseEntity<?> deleteReview(Integer reviewId);
}
