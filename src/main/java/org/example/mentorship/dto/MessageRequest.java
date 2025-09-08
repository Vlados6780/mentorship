package org.example.mentorship.dto;

public record MessageRequest(
        Integer chatId,
        String content
) {}
