package org.example.mentorship.service;

import org.example.mentorship.dto.*;

import java.util.List;

public interface ChatService {
    List<ChatDto> getCurrentUserChats();
    ChatDto createNewChat(NewChatRequest request);
    List<ChatMessageDto> getChatMessages(Integer chatId);
    ChatMessageDto sendMessage(MessageRequest messageRequest);
    void markMessagesAsRead(Integer chatId);
}
