package org.example.mentorship.dto;

import java.time.LocalDateTime;

public record ChatMessageDto(
        Integer messageId,
        Integer chatId,
        Integer senderId,
        String senderName,
        String senderProfilePictureUrl,
        String content,
        LocalDateTime sentAt,
        boolean read
) {}
