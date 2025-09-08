import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ProfileRequest, StudentInfoDto, MentorInfoDto } from '../models/profile-request.model';
import { UserProfile } from '../models/user-profile.model';

@Injectable({
  providedIn: 'root'
})
export class ProfileService {
  private apiUrl = 'http://localhost:8080/api/profile';
  private userApiUrl = 'http://localhost:8080/api/user';

  constructor(private http: HttpClient) { }

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
  }


  createProfile(
    profileData: ProfileRequest,
    studentInfo: StudentInfoDto | null,
    mentorInfo: MentorInfoDto | null,
    profilePicture: File
  ): Observable<string> {
    const formData = new FormData();

    formData.append('userId', profileData.userId.toString());
    formData.append('firstName', profileData.firstName);
    formData.append('lastName', profileData.lastName);
    formData.append('bio', profileData.bio);
    formData.append('age', profileData.age.toString());

    if (studentInfo) {
      formData.append('studentInfo', new Blob([JSON.stringify(studentInfo)], { type: 'application/json' }));
    }

    if (mentorInfo) {
      formData.append('mentorInfo', new Blob([JSON.stringify(mentorInfo)], { type: 'application/json' }));
    }

    formData.append('profilePicture', profilePicture);

    return this.http.post(
      `${this.apiUrl}/create`,
      formData,
      { responseType: 'text' }
    );
  }
  getUserProfile(): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${this.userApiUrl}/profile`, {
      headers: this.getAuthHeaders()
    });
  }
  updateUserProfile(updateData: any): Observable<any> {
    return this.http.put(`${this.userApiUrl}/update-profile`, updateData, {
      headers: this.getAuthHeaders()
    });
  }

  getProfilePicture(): Observable<{profilePictureUrl: string}> {
    return this.http.get<{profilePictureUrl: string}>(`${this.userApiUrl}/profile-picture`, {
      headers: this.getAuthHeaders()
    });
  }

  updateProfilePicture(file: File): Observable<any> {
    const formData = new FormData();
    formData.append('profilePicture', file);

    return this.http.post(`${this.userApiUrl}/update-profile-picture`, formData, {
      headers: this.getAuthHeaders()
    });
  }

  deleteAccount(): Observable<any> {
    return this.http.delete(`${this.apiUrl.replace('/profile', '/auth')}/delete`, {
      headers: this.getAuthHeaders()
    });
  }
}
