export interface ProfileRequest {
  userId: number;
  firstName: string;
  lastName: string;
  bio: string;
  age: number;
}

export interface StudentInfoDto {
  educationLevel: string;
  learningGoals: string;
}

export interface MentorInfoDto {
  hourlyRate: number;
  specialization: string;
  experienceYears: number;
  mentorTargetStudents: string;
}
