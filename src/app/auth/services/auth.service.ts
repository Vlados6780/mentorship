// src/app/auth/services/auth.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { LoginRequest } from '../models/login-request.model';
import { RegisterRequest } from '../models/register-request.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8080/api/auth';

  constructor(private http: HttpClient) {}

  login(loginRequest: LoginRequest): Observable<any> {
    return this.http.post(`${this.apiUrl}/login`, loginRequest, { responseType: 'text' })
      .pipe(
        tap(token => {
          localStorage.setItem('token', token);

          const role = this.getRoleFromToken(token);
          localStorage.setItem('userRole', role);
        })
      );
  }

  register(registerRequest: RegisterRequest): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/register`, registerRequest);
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('userRole');
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  getUserRole(): string | null {
    return localStorage.getItem('userRole');
  }

  private getRoleFromToken(token: string): string {
    if (!token) return '';

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.role || '';
    } catch (e) {
      console.error('Error decoding token', e);
      return '';
    }
  }

  resendVerificationEmail(email: string): Observable<any> {
    return this.http.post(`http://localhost:8080/api/verification/resend?email=${email}`, {});
  }

  confirmEmail(token: string): Observable<any> {
    return this.http.get(`http://localhost:8080/api/verification/confirm?token=${token}`);
  }
  getCurrentUserId(): number | null {
    const token = this.getToken();
    if (!token) return null;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.userId || null;
    } catch (e) {
      console.error('Error decoding token', e);
      return null;
    }
  }
}
