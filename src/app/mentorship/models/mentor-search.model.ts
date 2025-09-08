// src/app/mentorship/models/mentor-search.model.ts
export interface MentorSearchRequest {
  query?: string;
  specialization?: string;
  minRating?: number;
  maxRating?: number;
  minRate?: number;
  maxRate?: number;
  minExperience?: number;
  maxExperience?: number;
  sortBy?: string;
  sortDirection?: 'ASC' | 'DESC';
}
