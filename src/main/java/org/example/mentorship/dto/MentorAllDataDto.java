package org.example.mentorship.dto;

import java.math.BigDecimal;

public record MentorAllDataDto(
        Integer mentorId,
        String profilePictureUrl,
        String firstName,
        String lastName,
        String bio,
        Integer age,
        BigDecimal hourlyRate,
        String specialization,
        Integer experienceYears,
        BigDecimal averageRating,
        String mentorTargetStudents
) {}
