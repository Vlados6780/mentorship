package org.example.mentorship.repository;

import org.example.mentorship.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Integer> {
    List<ChatMessage> findByChatIdOrderBySentAtAsc(Integer chatId);
}
