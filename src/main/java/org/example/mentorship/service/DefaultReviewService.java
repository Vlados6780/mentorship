package org.example.mentorship.service;

import org.example.mentorship.dto.ReviewRequest;
import org.example.mentorship.dto.ReviewResponse;
import org.example.mentorship.dto.ReviewUpdateRequest;
import org.example.mentorship.entity.*;
import org.example.mentorship.repository.MentorRepository;
import org.example.mentorship.repository.ReviewRepository;
import org.example.mentorship.repository.StudentRepository;
import org.example.mentorship.security.CurrentUserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DefaultReviewService implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final MentorRepository mentorRepository;
    private final StudentRepository studentRepository;
    private final CurrentUserProvider currentUserProvider;

    @Autowired
    public DefaultReviewService(
            ReviewRepository reviewRepository,
            MentorRepository mentorRepository,
            StudentRepository studentRepository,
            CurrentUserProvider currentUserProvider) {
        this.reviewRepository = reviewRepository;
        this.mentorRepository = mentorRepository;
        this.studentRepository = studentRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    @Transactional
    public ResponseEntity<?> createReview(ReviewRequest reviewRequest) {
        return currentUserProvider.withCurrentUser(currentUser -> {
            // Check that the user is a student
            Optional<Student> studentOpt = studentRepository.findByUser(currentUser);
            if (studentOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Only students can leave reviews");
            }

            Student student = studentOpt.get();

            // Get the mentor
            Optional<Mentor> mentorOpt = mentorRepository.findById(reviewRequest.mentorId());
            if (mentorOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Mentor not found");
            }

            Mentor mentor = mentorOpt.get();

            // Check that the rating is in the range from 1 to 5
            if (reviewRequest.rating() < 1 || reviewRequest.rating() > 5) {
                return ResponseEntity.badRequest().body("Rating must be from 1 to 5");
            }

            // Check if there is already a review from this student for this mentor
            Optional<Review> existingReview = reviewRepository.findByStudentAndMentor(student, mentor);

            Review review;
            if (existingReview.isPresent()) {
                // Update existing review
                review = existingReview.get();
                review.setComment(reviewRequest.comment());
                review.setRating(reviewRequest.rating());
            } else {
                // Create new review
                review = new Review();
                review.setStudent(student);
                review.setMentor(mentor);
                review.setComment(reviewRequest.comment());
                review.setRating(reviewRequest.rating());
            }

            // Save the review
            reviewRepository.save(review);

            // Update the mentor's average rating
            updateMentorAverageRating(mentor);

            return ResponseEntity.ok().body("Review successfully saved");
        });
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<List<ReviewResponse>> getMentorReviews(Integer mentorId) {
        // Get the mentor
        Optional<Mentor> mentorOpt = mentorRepository.findById(mentorId);
        if (mentorOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        Mentor mentor = mentorOpt.get();

        // Get all reviews for the mentor
        List<Review> reviews = reviewRepository.findByMentorOrderByCreatedAtDesc(mentor);
        List<ReviewResponse> reviewResponses = new ArrayList<>();

        for (Review review : reviews) {
            Student student = review.getStudent();
            User studentUser = student.getUser();
            Profile studentProfile = studentUser.getProfile();

            String firstName = "";
            String lastName = "";
            String profilePicture = "";

            if (studentProfile != null) {
                firstName = studentProfile.getFirstName();
                lastName = studentProfile.getLastName();
                profilePicture = studentProfile.getProfilePictureUrl();
            }

            ReviewResponse reviewResponse = new ReviewResponse(
                    review.getId(),
                    profilePicture,
                    firstName,
                    lastName,
                    review.getComment(),
                    review.getRating(),
                    review.getCreatedAt()
            );

            reviewResponses.add(reviewResponse);
        }

        return ResponseEntity.ok(reviewResponses);
    }

    @Transactional
    protected void updateMentorAverageRating(Mentor mentor) {
        // Get the average rating from the database
        Double averageRating = reviewRepository.calculateAverageRatingForMentor(mentor);

        if (averageRating != null) {
            // Round to two decimal places
            BigDecimal bd = new BigDecimal(averageRating).setScale(2, RoundingMode.HALF_UP);
            mentor.setAverageRating(bd);
        } else {
            // If there are no reviews, set rating to 0
            mentor.setAverageRating(BigDecimal.ZERO);
        }

        // Save the updated mentor rating
        mentorRepository.save(mentor);
    }

    @Override
    @Transactional
    public ResponseEntity<?> updateReview(Integer reviewId, ReviewUpdateRequest reviewRequest) {
        return currentUserProvider.withCurrentUser(currentUser -> {
            // Check that the user is a student
            Optional<Student> studentOpt = studentRepository.findByUser(currentUser);
            if (studentOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Only students can update reviews");
            }

            Student student = studentOpt.get();

            // Find the review
            Optional<Review> reviewOpt = reviewRepository.findById(reviewId);
            if (reviewOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Review review = reviewOpt.get();

            // Check that the student is the author of the review
            if (!review.getStudent().getId().equals(student.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You can only update your own reviews");
            }

            // Check that the rating is in the range from 1 to 5
            if (reviewRequest.rating() < 1 || reviewRequest.rating() > 5) {
                return ResponseEntity.badRequest().body("Rating must be from 1 to 5");
            }

            // Update the review
            review.setComment(reviewRequest.comment());
            review.setRating(reviewRequest.rating());
            reviewRepository.save(review);

            // Update the mentor's average rating
            updateMentorAverageRating(review.getMentor());

            return ResponseEntity.ok().body("Review successfully updated");
        });
    }

    @Override
    @Transactional
    public ResponseEntity<?> deleteReview(Integer reviewId) {
        return currentUserProvider.withCurrentUser(currentUser -> {
            // Check that the user is a student
            Optional<Student> studentOpt = studentRepository.findByUser(currentUser);
            if (studentOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Only students can delete reviews");
            }

            Student student = studentOpt.get();

            // Find the review
            Optional<Review> reviewOpt = reviewRepository.findById(reviewId);
            if (reviewOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Review review = reviewOpt.get();

            // Check that the student is the author of the review
            if (!review.getStudent().getId().equals(student.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You can only delete your own reviews");
            }

            // Save the mentor for updating the rating after deletion
            Mentor mentor = review.getMentor();

            // Delete the review
            reviewRepository.delete(review);

            // Update the mentor's average rating
            updateMentorAverageRating(mentor);

            return ResponseEntity.ok().body("Review successfully deleted");
        });
    }
}
