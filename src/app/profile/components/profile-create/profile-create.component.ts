import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ProfileService } from '../../services/profile.service';
import { AuthService } from '../../../auth/services/auth.service';
import { ProfileRequest, StudentInfoDto, MentorInfoDto } from '../../models/profile-request.model';

@Component({
  selector: 'app-profile-create',
  templateUrl: './profile-create.component.html',
  styleUrls: ['./profile-create.component.css'],
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule]
})
export class ProfileCreateComponent implements OnInit {
  profileForm: FormGroup;
  selectedFile: File | null = null;
  errorMessage = '';
  userId: number | null = null;
  userRole: string = '';

  constructor(
    private fb: FormBuilder,
    private profileService: ProfileService,
    private authService: AuthService,
    private router: Router
  ) {
    this.profileForm = this.fb.group({
      firstName: ['', [Validators.required, Validators.maxLength(100)]],
      lastName: ['', [Validators.required, Validators.maxLength(100)]],
      bio: ['', [Validators.required, Validators.maxLength(500)]],
      age: ['', [Validators.required, Validators.min(18), Validators.max(120)]],
      // Fields for student
      educationLevel: [''],
      learningGoals: [''],
      // Fields for mentor
      hourlyRate: [''],
      specialization: [''],
      experienceYears: [''],
      mentorTargetStudents: ['']
    });
  }

  ngOnInit(): void {
    this.userId = this.getUserIdFromToken();
    this.userRole = localStorage.getItem('userRole') || '';

    if (!this.userId) {
      this.errorMessage = 'Failed to retrieve user ID. Please register again.';
      setTimeout(() => {
        this.router.navigate(['/register']);
      }, 2000);
      return;
    }

    // Add validators based on role
    if (this.userRole === 'ROLE_STUDENT') {
      this.profileForm.get('educationLevel')?.setValidators([Validators.required]);
      this.profileForm.get('learningGoals')?.setValidators([Validators.required]);
    } else if (this.userRole === 'ROLE_MENTOR') {
      this.profileForm.get('hourlyRate')?.setValidators([Validators.required, Validators.min(0)]);
      this.profileForm.get('specialization')?.setValidators([Validators.required]);
      this.profileForm.get('experienceYears')?.setValidators([Validators.required, Validators.min(0)]);
      this.profileForm.get('mentorTargetStudents')?.setValidators([Validators.required]);
    }

    // Update validators
    this.profileForm.get('educationLevel')?.updateValueAndValidity();
    this.profileForm.get('learningGoals')?.updateValueAndValidity();
    this.profileForm.get('hourlyRate')?.updateValueAndValidity();
    this.profileForm.get('specialization')?.updateValueAndValidity();
    this.profileForm.get('experienceYears')?.updateValueAndValidity();
    this.profileForm.get('mentorTargetStudents')?.updateValueAndValidity();
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length) {
      this.selectedFile = input.files[0];
    }
  }

  onSubmit(): void {
    if (this.profileForm.invalid || !this.selectedFile || !this.userId) {
      return;
    }

    // Basic profile data
    const profileData: ProfileRequest = {
      userId: this.userId,
      firstName: this.profileForm.value.firstName,
      lastName: this.profileForm.value.lastName,
      bio: this.profileForm.value.bio,
      age: Number(this.profileForm.value.age)
    };

    // Create data based on role
    let studentInfo: StudentInfoDto | null = null;
    let mentorInfo: MentorInfoDto | null = null;

    if (this.userRole === 'ROLE_STUDENT') {
      studentInfo = {
        educationLevel: this.profileForm.value.educationLevel,
        learningGoals: this.profileForm.value.learningGoals
      };
    } else if (this.userRole === 'ROLE_MENTOR') {
      mentorInfo = {
        hourlyRate: Number(this.profileForm.value.hourlyRate),
        specialization: this.profileForm.value.specialization,
        experienceYears: Number(this.profileForm.value.experienceYears),
        mentorTargetStudents: this.profileForm.value.mentorTargetStudents
      };
    }

    this.profileService.createProfile(profileData, studentInfo, mentorInfo, this.selectedFile)
      .subscribe({
        next: () => {
          const userEmail = localStorage.getItem('userEmail');
          if (userEmail) {
            localStorage.setItem('verificationEmail', userEmail);
          }
          localStorage.removeItem('userId');
          this.router.navigate(['/verify-email']);
        },
        error: (err) => {
          console.error('Backend error:', err);
          this.errorMessage = err.error || 'Error creating profile';
        }
      });
  }

  private getUserIdFromToken(): number | null {
    const userId = localStorage.getItem('userId');
    if (userId) {
      return parseInt(userId, 10);
    }
    return null;
  }

  isStudent(): boolean {
    return this.userRole === 'ROLE_STUDENT';
  }

  isMentor(): boolean {
    return this.userRole === 'ROLE_MENTOR';
  }
}
