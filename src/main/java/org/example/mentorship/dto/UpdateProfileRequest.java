package org.example.mentorship.dto;

import java.math.BigDecimal;

public record UpdateProfileRequest(

        String firstName,
        String lastName,
        String bio,
        Integer age,

        String educationLevel,
        String learningGoals,

        BigDecimal hourlyRate,
        String specialization,
        Integer experienceYears,
        String mentorTargetStudents
) {}
