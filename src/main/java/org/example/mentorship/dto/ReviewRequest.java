package org.example.mentorship.dto;

public record ReviewRequest(
         Integer mentorId,
         String comment,
         Integer rating
) {
}
