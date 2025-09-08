// src/app/auth/components/email-verification/email-verification.component.ts
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-email-verification',
  templateUrl: './email-verification.component.html',
  styleUrls: ['./email-verification.component.css'],
  standalone: true,
  imports: [CommonModule]
})
export class EmailVerificationComponent implements OnInit {
  email: string = '';
  message: string = '';
  isResending: boolean = false;
  token: string | null = null;
  isConfirmed: boolean = false;

  constructor(
    private authService: AuthService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.email = localStorage.getItem('verificationEmail') || '';
    this.token = this.route.snapshot.queryParamMap.get('token');

    if (this.token) {
      this.confirmEmail(this.token);
    }
  }

  resendEmail(): void {
    if (!this.email || this.isResending) {
      return;
    }

    this.isResending = true;
    this.authService.resendVerificationEmail(this.email).subscribe({
      next: (response) => {
        this.message = 'Verification email sent again';
        this.isResending = false;
      },
      error: (error) => {
        this.message = error.error?.message || 'Error resending the email';
        this.isResending = false;
      }
    });
  }

  private confirmEmail(token: string): void {
    this.authService.confirmEmail(token).subscribe({
      next: (response) => {
        this.message = 'Email successfully verified';
        this.isConfirmed = true;
        // Redirect after 4 seconds
        setTimeout(() => this.router.navigate(['/login']), 4000);
      },
      error: (error) => {
        this.message = error.error?.message || 'Invalid or expired token';
      }
    });
  }
}
