// src/app/mentorship/models/review.model.ts
export interface Review {
  id: number;
  mentorId: number;
  studentId: number;
  studentFirstName: string;
  studentLastName: string;
  studentProfilePictureUrl?: string;
  rating: number;
  comment: string;
  createdAt: string;
  updatedAt: string;
}

export interface ReviewRequest {
  mentorId: number;
  comment: string;
  rating: number;
}

export interface ReviewUpdateRequest {
  comment: string;
  rating: number;
}
