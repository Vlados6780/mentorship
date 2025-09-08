package org.example.mentorship.dto;

import java.time.LocalDateTime;

public record ReviewResponse(
        Integer id,
        String studentProfilePicture,
        String studentFirstName,
        String studentLastName,
        String comment,
        Integer rating,
        LocalDateTime createdAt
) {
}
