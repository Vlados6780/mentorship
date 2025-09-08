import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MentorshipService } from '../../services/mentorship.service';
import { AuthService } from '../../../auth/services/auth.service';
import { ErrorModalComponent } from '../../../shared/components/error-modal/error-modal.component';
import { Review, ReviewRequest, ReviewUpdateRequest } from '../../models/review.model';
import {Router} from '@angular/router'; // Assuming models are defined

@Component({
  selector: 'app-mentor-reviews',
  templateUrl: './mentor-reviews.component.html',
  styleUrls: ['./mentor-reviews.component.css'],
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, ErrorModalComponent]
})
export class MentorReviewsComponent implements OnInit {
  @Input() mentorId!: number;
  @Input() mentorName!: string;
  @Output() close = new EventEmitter<void>(); // Changed to match usage in parent (close event)

  reviews: Review[] = [];
  isLoading = true;
  errorMessage = '';
  showErrorModal = false;

  reviewForm!: FormGroup;
  isSubmitting = false;
  canAddReview = false;

  // Variables for editing
  editingReviewId: number | null = null;
  editForm!: FormGroup;
  isEditing = false;

  constructor(
    private mentorshipService: MentorshipService,
    public authService: AuthService,
    private fb: FormBuilder,
    private router: Router // Assuming Router is needed, but not used; keep if necessary
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.initEditForm();
    this.loadReviews();
    this.checkUserPermissions();
  }

  initForm(): void {
    this.reviewForm = this.fb.group({
      rating: [5, [Validators.required, Validators.min(1), Validators.max(5)]],
      comment: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(500)]]
    });
  }

  // Initialize edit form
  initEditForm(): void {
    this.editForm = this.fb.group({
      rating: [5, [Validators.required, Validators.min(1), Validators.max(5)]],
      comment: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(500)]]
    });
  }

  setRating(value: number): void {
    this.reviewForm.get('rating')!.setValue(value);
  }

  // Set rating for edit form
  setEditRating(value: number): void {
    this.editForm.get('rating')!.setValue(value);
  }

  loadReviews(): void {
    this.isLoading = true;
    this.mentorshipService.getMentorReviews(this.mentorId).subscribe({
      next: (reviews) => {
        this.reviews = reviews;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading reviews:', error);
        this.errorMessage = 'Failed to load reviews';
        this.showErrorModal = true;
        this.isLoading = false;
      }
    });
  }

  checkUserPermissions(): void {
    this.canAddReview = this.authService.isAuthenticated() &&
      this.authService.getUserRole() === 'ROLE_STUDENT';
  }

  submitReview(): void {
    if (this.reviewForm.invalid) {
      return;
    }

    const reviewRequest: ReviewRequest = {
      mentorId: this.mentorId,
      rating: this.reviewForm.value.rating,
      comment: this.reviewForm.value.comment
    };

    this.isSubmitting = true;

    this.mentorshipService.createReview(reviewRequest)
      .subscribe({
        next: () => {
          console.log('Review submitted successfully');
          this.loadReviews(); // Reload reviews
          this.reviewForm.reset({ rating: 5 }); // Reset form
          this.isSubmitting = false;
          this.close.emit(); // Close modal after submit to refresh parent
        },
        error: (error) => {
          console.error('Error submitting review:', error);
          this.errorMessage = 'Unable to submit review';
          this.showErrorModal = true;
          this.isSubmitting = false;
        }
      });
  }

  // Method to start editing a review
  startEditReview(review: Review): void {
    if (this.authService.getCurrentUserId() !== review.studentId) {
      this.errorMessage = 'You can only edit your own reviews';
      this.showErrorModal = true;
      return;
    }
    this.editingReviewId = review.id;
    this.editForm.setValue({
      rating: review.rating,
      comment: review.comment
    });
  }

  // Method to cancel editing
  cancelEdit(): void {
    this.editingReviewId = null;
    this.editForm.reset();
    this.isEditing = false;
  }

  // Method to save edited review
  saveEditedReview(): void {
    if (this.editForm.invalid || !this.editingReviewId) {
      return;
    }

    const updateRequest: ReviewUpdateRequest = {
      rating: this.editForm.value.rating,
      comment: this.editForm.value.comment
    };

    this.isEditing = true;

    this.mentorshipService.updateReview(this.editingReviewId, updateRequest)
      .subscribe({
        next: (updatedReview) => {
          console.log('Review updated successfully');
          this.loadReviews(); // Reload reviews
          this.editingReviewId = null;
          this.isEditing = false;
          this.close.emit(); // Close modal after edit to refresh parent
        },
        error: (error) => {
          console.error('Error updating review:', error);
          this.errorMessage = 'Unable to update review';
          this.showErrorModal = true;
          this.isEditing = false;
        }
      });
  }

  deleteReview(reviewId: number): void {
    if (!confirm('Are you sure you want to delete this review?')) {
      return;
    }

    if (this.authService.getCurrentUserId() !== this.reviews.find(r => r.id === reviewId)?.studentId) {
      this.errorMessage = 'You can only delete your own reviews';
      this.showErrorModal = true;
      return;
    }

    this.mentorshipService.deleteReview(reviewId).subscribe({
      next: () => {
        this.loadReviews(); // Reload to ensure consistency and update average
        console.log('Review deleted successfully');
      },
      error: (error) => {
        console.error('Error deleting review:', error);
        this.errorMessage = 'Unable to delete review';
        this.showErrorModal = true;
      }
    });
  }

  getRatingStars(rating: number): string[] {
    const fullStars = Math.floor(rating);
    const halfStar = rating % 1 >= 0.5;
    const emptyStars = 5 - fullStars - (halfStar ? 1 : 0);

    return [
      ...Array(fullStars).fill('full'),
      ...(halfStar ? ['half'] : []),
      ...Array(emptyStars).fill('empty')
    ];
  }

  closeErrorModal(): void {
    this.showErrorModal = false;
    this.errorMessage = '';
  }
}
