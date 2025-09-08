package org.example.mentorship.dto;

import java.time.LocalDateTime;

public record WebSocketMessageDto(
        String type, // MESSAGE, TYPING, READ
        Integer chatId,
        Integer senderId,
        String senderName,
        String content,
        LocalDateTime sentAt,
        Integer messageId
) {}
