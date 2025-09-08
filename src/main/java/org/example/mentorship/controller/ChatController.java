package org.example.mentorship.controller;

import org.example.mentorship.dto.*;
import org.example.mentorship.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final AsyncTaskExecutor executor;
    private final ChatService chatService;

    @Autowired
    public ChatController(
            @Qualifier("securityAwareAsyncExecutor") AsyncTaskExecutor executor,
            ChatService chatService) {
        this.executor = executor;
        this.chatService = chatService;
    }

    @GetMapping("/list")
    public CompletableFuture<ResponseEntity<List<ChatDto>>> getCurrentUserChats() {
        return CompletableFuture.supplyAsync(() -> {
            List<ChatDto> chats = chatService.getCurrentUserChats();
            return ResponseEntity.ok(chats);
        }, executor);
    }

    @PostMapping("/create")
    public CompletableFuture<ResponseEntity<ChatDto>> createChat(@RequestBody NewChatRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            ChatDto chat = chatService.createNewChat(request);
            return ResponseEntity.ok(chat);
        }, executor);
    }

    @GetMapping("/{chatId}/messages")
    public CompletableFuture<ResponseEntity<List<ChatMessageDto>>> getChatMessages(@PathVariable Integer chatId) {
        return CompletableFuture.supplyAsync(() -> {
            List<ChatMessageDto> messages = chatService.getChatMessages(chatId);
            return ResponseEntity.ok(messages);
        }, executor);
    }

    @PostMapping("/send")
    public CompletableFuture<ResponseEntity<ChatMessageDto>> sendMessage(@RequestBody MessageRequest messageRequest) {
        return CompletableFuture.supplyAsync(() -> {
            ChatMessageDto message = chatService.sendMessage(messageRequest);
            return ResponseEntity.ok(message);
        }, executor);
    }

    @PostMapping("/{chatId}/read")
    public CompletableFuture<ResponseEntity<Void>> markMessagesAsRead(@PathVariable Integer chatId) {
        return CompletableFuture.supplyAsync(() -> {
            chatService.markMessagesAsRead(chatId);
            return ResponseEntity.ok().build();
        }, executor);
    }

}
