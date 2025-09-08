// src/app/profile/components/profile-view/profile-view.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ProfileService } from '../../services/profile.service';
import { AuthService } from '../../../auth/services/auth.service';
import { UserProfile } from '../../models/user-profile.model';

@Component({
  selector: 'app-profile-view',
  templateUrl: './profile-view.component.html',
  styleUrls: ['./profile-view.component.css'],
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule]
})
export class ProfileViewComponent implements OnInit {
  profile: UserProfile | null = null;
  isLoading = true;
  errorMessage = '';
  userRole = '';
  userEmail = '';
  profilePictureUrl = '';
  isEditFormOpen = false;
  isDeleteConfirmOpen = false;
  updateForm: FormGroup;
  selectedProfilePicture: File | null = null;

  constructor(
    private profileService: ProfileService,
    private authService: AuthService,
    public router: Router,
    private fb: FormBuilder
  ) {
    this.updateForm = this.fb.group({
      firstName: ['', Validators.required],
      lastName: [''],
      bio: [''],
      age: [null],
      educationLevel: [''],
      learningGoals: [''],
      hourlyRate: [null],
      specialization: [''],
      experienceYears: [null],
      mentorTargetStudents: ['']
    });
  }

  ngOnInit(): void {
    if (!this.authService.isAuthenticated()) {
      this.redirectToLogin('Authorization required');
      return;
    }

    this.userRole = this.authService.getUserRole() || '';
    this.extractEmailFromToken();
    this.loadProfile();
    this.loadProfilePicture();
  }

  private extractEmailFromToken(): void {
    const token = localStorage.getItem('token');
    if (token) {
      try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        this.userEmail = payload.sub || '';
      } catch (e) {
        console.error('Error decoding token', e);
      }
    }
  }

  loadProfile(): void {
    this.isLoading = true;
    this.profileService.getUserProfile().subscribe({
      next: (profile) => {
        this.profile = profile;
        this.isLoading = false;
        this.populateUpdateForm();
      },
      error: (error) => {
        console.error('Error fetching profile:', error);
        this.isLoading = false;
        if (error?.status === 401 || error?.status === 403) {
          this.redirectToLogin('Authorization required. Redirecting to login page...');
        } else {
          this.errorMessage = 'Error fetching profile.';
        }
      }
    });
  }

  loadProfilePicture(): void {
    this.profileService.getProfilePicture().subscribe({
      next: (response) => {
        this.profilePictureUrl = response.profilePictureUrl;
      },
      error: (error) => {
        console.error('Error fetching profile picture:', error);
      }
    });
  }

  populateUpdateForm(): void {
    if (!this.profile) return;

    this.updateForm.patchValue({
      firstName: this.profile.firstName,
      lastName: this.profile.lastName,
      bio: this.profile.bio,
      age: this.profile.age,
      educationLevel: this.profile.educationLevel,
      learningGoals: this.profile.learningGoals,
      hourlyRate: this.profile.hourlyRate,
      specialization: this.profile.specialization,
      experienceYears: this.profile.experienceYears,
      mentorTargetStudents: this.profile.mentorTargetStudents
    });
  }

  toggleEditForm(): void {
    this.isEditFormOpen = !this.isEditFormOpen;
    if (this.isEditFormOpen) {
      this.populateUpdateForm();
    }
  }

  onProfilePictureChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length) {
      this.selectedProfilePicture = input.files[0];
      this.updateProfilePicture();
    }
  }

  updateProfilePicture(): void {
    if (!this.selectedProfilePicture) return;

    this.profileService.updateProfilePicture(this.selectedProfilePicture).subscribe({
      next: () => {
        // Reload profile picture after successful update
        this.loadProfilePicture();
      },
      error: (error) => {
        console.error('Error updating profile picture:', error);
        this.errorMessage = 'Failed to update profile picture.';
      }
    });
  }

  onUpdateSubmit(): void {
    if (this.updateForm.invalid) return;

    const updateData = {
      ...this.updateForm.value
    };

    this.profileService.updateUserProfile(updateData).subscribe({
      next: () => {
        this.isEditFormOpen = false;
        this.loadProfile();
      },
      error: (error) => {
        console.error('Error updating profile:', error);
        this.errorMessage = 'Failed to update profile.';
      }
    });
  }

  toggleDeleteConfirm(): void {
    this.isDeleteConfirmOpen = !this.isDeleteConfirmOpen;
  }

  confirmDelete(): void {
    this.profileService.deleteAccount().subscribe({
      next: () => {
        this.authService.logout();
        this.router.navigate(['/login']);
      },
      error: (error) => {
        console.error('Error deleting account:', error);
        this.errorMessage = 'Failed to delete account.';
        this.isDeleteConfirmOpen = false;
      }
    });
  }

  redirectToLogin(message: string): void {
    this.errorMessage = message;
    this.authService.logout();
    setTimeout(() => this.router.navigate(['/login']), 1500);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  isStudent(): boolean {
    return this.userRole === 'ROLE_STUDENT';
  }

  isMentor(): boolean {
    return this.userRole === 'ROLE_MENTOR';
  }

  getRoleDisplay(): string {
    return this.isStudent() ? 'student' : this.isMentor() ? 'mentor' : '';
  }
}
