import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { MentorshipService } from '../../services/mentorship.service';
import { MentorAllDataDto } from '../../models/mentor.model';
import { AuthService } from '../../../auth/services/auth.service';
import { MentorSearchComponent } from '../mentor-search/mentor-search.component';
import { ReviewModalComponent } from '../review-modal/review-modal.component';
import { ErrorModalComponent } from '../../../shared/components/error-modal/error-modal.component';

@Component({
  selector: 'app-mentor-list',
  templateUrl: './mentor-list.component.html',
  styleUrls: ['./mentor-list.component.css'],
  standalone: true,
  imports: [CommonModule, RouterModule, ErrorModalComponent, MentorSearchComponent, ReviewModalComponent]
})
export class MentorListComponent implements OnInit {
  mentors: MentorAllDataDto[] = [];
  isLoading = true;
  errorMessage = '';
  showErrorModal = false;

  // Explicitly type for clarity (matches usage)
  selectedMentorForReviews: { id: number; name: string } | null = null;

  constructor(
    private mentorshipService: MentorshipService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadMentors();
  }

  loadMentors(): void {
    this.mentorshipService.getAllMentors().subscribe({
      next: (mentors) => {
        this.mentors = mentors;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading mentor list:', error);
        this.errorMessage = 'Failed to load mentor list';
        this.isLoading = false;
        this.showErrorModal = true;
      }
    });
  }

  connectWithMentor(mentorId: number): void {
    if (!this.authService.isAuthenticated()) {
      localStorage.setItem('redirectAfterLogin', `/chat/new/${mentorId}`);
      this.router.navigate(['/login']);
      return;
    }

    const userRole = this.authService.getUserRole();

    if (userRole === 'ROLE_MENTOR') {
      this.errorMessage = 'Mentors cannot create chats with other mentors.';
      this.showErrorModal = true;
      return;
    }

    // If the user is a student, create chat
    this.router.navigate(['/chat/new', mentorId]);
  }

  closeErrorModal(): void {
    this.showErrorModal = false;
    this.errorMessage = '';
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

  handleSearchResults(results: MentorAllDataDto[]): void {
    this.mentors = results;
  }

  handleSearching(isSearching: boolean): void {
    this.isLoading = isSearching;
  }

  openReviewsModal(mentor: MentorAllDataDto): void {
    if (!mentor) return;
    this.selectedMentorForReviews = {
      id: mentor.mentorId,
      name: `${mentor.firstName} ${mentor.lastName}`
    };
  }

  closeReviewsModal(): void {
    this.selectedMentorForReviews = null;
    // Reload mentors to update average ratings after review changes
    this.loadMentors();
  }
}
