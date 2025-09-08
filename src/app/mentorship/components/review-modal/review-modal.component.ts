// src/app/mentorship/components/review-modal/review-modal.component.ts
import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MentorReviewsComponent } from '../mentor-reviews/mentor-reviews.component';

@Component({
  selector: 'app-review-modal',
  templateUrl: './review-modal.component.html',
  styleUrls: ['./review-modal.component.css'],
  standalone: true,
  imports: [CommonModule, MentorReviewsComponent]
})
export class ReviewModalComponent {
  @Input() mentorId!: number;
  @Input() mentorName!: string;
  @Output() close = new EventEmitter<void>();

  onClose(): void {
    this.close.emit();
  }
}
