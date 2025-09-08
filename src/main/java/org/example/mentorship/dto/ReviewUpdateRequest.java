package org.example.mentorship.dto;

public record ReviewUpdateRequest(
        String comment,
        Integer rating
) {}
