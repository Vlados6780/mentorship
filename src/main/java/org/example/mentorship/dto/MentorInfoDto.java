package org.example.mentorship.dto;

import java.math.BigDecimal;

public record MentorInfoDto(
        BigDecimal hourlyRate,
        String specialization,
        Integer experienceYears,
        String mentorTargetStudents
) {}
