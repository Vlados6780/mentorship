// src/app/mentorship/services/mentorship.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MentorAllDataDto } from '../models/mentor.model';
import { MentorSearchRequest } from '../models/mentor-search.model';
import { Review } from '../models/review.model';
import { ReviewRequest, ReviewUpdateRequest } from '../models/review.model';

@Injectable({
  providedIn: 'root'
})
export class MentorshipService {
  private apiUrl = 'http://localhost:8080/api/guest';
  private apiBaseUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) { }

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
  }

  getAllMentors(): Observable<MentorAllDataDto[]> {
    return this.http.get<MentorAllDataDto[]>(`${this.apiUrl}/mentors`);
  }

  searchMentors(searchRequest: MentorSearchRequest): Observable<MentorAllDataDto[]> {
    return this.http.post<MentorAllDataDto[]>(`${this.apiUrl}/mentors/search`, searchRequest);
  }

  getMentorReviews(mentorId: number): Observable<Review[]> {
    return this.http.get<Review[]>(`${this.apiBaseUrl}/reviews/mentor/${mentorId}`);
  }

  createReview(reviewRequest: ReviewRequest): Observable<any> {
    return this.http.post(`${this.apiBaseUrl}/reviews`, reviewRequest, {
      headers: this.getAuthHeaders(),
      responseType: 'text'
    });
  }

  updateReview(reviewId: number, updateRequest: ReviewUpdateRequest): Observable<Review> {
    return this.http.put<Review>(`${this.apiBaseUrl}/reviews/${reviewId}`, updateRequest, {
      headers: this.getAuthHeaders()
    });
  }

  deleteReview(reviewId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiBaseUrl}/reviews/${reviewId}`, {
      headers: this.getAuthHeaders()
    });
  }
}
