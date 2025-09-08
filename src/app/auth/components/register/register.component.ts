import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css'],
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule]
})
export class RegisterComponent {
  registerForm: FormGroup;
  errorMessage = '';
  roles = [
    { label: 'Student', value: 'ROLE_STUDENT' },
    { label: 'Mentor', value: 'ROLE_MENTOR' }
  ];

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.registerForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      role: ['ROLE_STUDENT', Validators.required]
    });
  }

  onSubmit(): void {
    if (this.registerForm.invalid) {
      return;
    }

    this.errorMessage = '';

    this.authService.register(this.registerForm.value)
      .subscribe({
        next: (res) => {
          // Save userId, email, and role in localStorage
          localStorage.setItem('userId', res.userId.toString());
          localStorage.setItem('userEmail', this.registerForm.value.email);
          localStorage.setItem('userRole', this.registerForm.value.role);
          this.router.navigate(['/create-profile']);
        },
        error: (err) => {
          this.errorMessage = err.error || 'Registration error';
        }
      });
  }
}
