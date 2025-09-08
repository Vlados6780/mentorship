package org.example.mentorship.dto;

import java.time.LocalDateTime;

public record ChatDto(
        Integer chatId,
        Integer studentId,
        String studentName,
        String studentPictureUrl,
        Integer mentorId,
        String mentorName,
        String mentorPictureUrl,
        LocalDateTime lastMessageTime,
        String lastMessageContent,
        long unreadCount
) {}