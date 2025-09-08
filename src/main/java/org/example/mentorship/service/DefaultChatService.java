package org.example.mentorship.service;

import org.example.mentorship.dto.*;
import org.example.mentorship.entity.Chat;
import org.example.mentorship.entity.ChatMessage;
import org.example.mentorship.entity.Mentor;
import org.example.mentorship.entity.Profile;
import org.example.mentorship.entity.Student;
import org.example.mentorship.entity.User;
import org.example.mentorship.repository.*;
import org.example.mentorship.security.CurrentUserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DefaultChatService implements ChatService {

    private final ChatRepository chatRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final StudentRepository studentRepository;
    private final MentorRepository mentorRepository;
    private final ProfileRepository profileRepository;
    private final CurrentUserProvider currentUserProvider;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public DefaultChatService(ChatRepository chatRepository,
                              ChatMessageRepository chatMessageRepository,
                              StudentRepository studentRepository,
                              MentorRepository mentorRepository,
                              ProfileRepository profileRepository,
                              CurrentUserProvider currentUserProvider,
                              SimpMessagingTemplate messagingTemplate) {
        this.chatRepository = chatRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.studentRepository = studentRepository;
        this.mentorRepository = mentorRepository;
        this.profileRepository = profileRepository;
        this.currentUserProvider = currentUserProvider;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatDto> getCurrentUserChats() {
        return currentUserProvider.getCurrentUser().map(user -> {
            List<Chat> chats;
            if ("ROLE_STUDENT".equals(user.getRole().getRoleName())) {
                Student student = studentRepository.findByUser(user)
                        .orElseThrow(() -> new RuntimeException("Student not found"));
                chats = chatRepository.findByStudentId(student.getId());
            } else if ("ROLE_MENTOR".equals(user.getRole().getRoleName())) {
                Mentor mentor = mentorRepository.findByUser(user)
                        .orElseThrow(() -> new RuntimeException("Mentor not found"));
                chats = chatRepository.findByMentorId(mentor.getId());
            } else {
                throw new RuntimeException("Invalid user role");
            }
            return chats.stream()
                    .map(this::mapChatToDTO)
                    .sorted(Comparator.comparing(ChatDto::lastMessageTime).reversed())
                    .collect(Collectors.toList());
        }).orElseThrow(() -> new RuntimeException("User not authenticated"));
    }

    @Override
    @Transactional
    public ChatDto createNewChat(NewChatRequest request) {
        return currentUserProvider.getCurrentUser().map(user -> {
            if (!"ROLE_STUDENT".equals(user.getRole().getRoleName())) {
                throw new RuntimeException("Only students can create chats");
            }

            Student student = studentRepository.findByUser(user)
                    .orElseThrow(() -> new RuntimeException("Student not found"));

            Mentor mentor = mentorRepository.findById(request.mentorId())
                    .orElseThrow(() -> new RuntimeException("Mentor not found"));

            // Check if a chat already exists between this student and mentor
            return chatRepository.findByStudentIdAndMentorId(student.getId(), mentor.getId())
                    .map(this::mapChatToDTO)
                    .orElseGet(() -> {
                        // Create a new chat
                        Chat newChat = new Chat();
                        newChat.setStudent(student);
                        newChat.setMentor(mentor);
                        newChat.setCreatedAt(LocalDateTime.now());
                        newChat.setUpdatedAt(LocalDateTime.now());

                        Chat savedChat = chatRepository.save(newChat);
                        return mapChatToDTO(savedChat);
                    });
        }).orElseThrow(() -> new RuntimeException("User not authenticated"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageDto> getChatMessages(Integer chatId) {
        return currentUserProvider.getCurrentUser().map(user -> {
            Chat chat = chatRepository.findById(chatId)
                    .orElseThrow(() -> new RuntimeException("Chat not found"));

            // Check access to the chat
            if (!userHasAccessToChat(user, chat)) {
                throw new RuntimeException("You do not have access to this chat");
            }

            return chatMessageRepository.findByChatIdOrderBySentAtAsc(chatId).stream()
                    .map(this::mapChatMessageToDTO)
                    .collect(Collectors.toList());
        }).orElseThrow(() -> new RuntimeException("User not authenticated"));
    }

    @Override
    @Transactional
    public ChatMessageDto sendMessage(MessageRequest messageRequest) {
        return currentUserProvider.getCurrentUser().map(user -> {
            Chat chat = chatRepository.findById(messageRequest.chatId())
                    .orElseThrow(() -> new RuntimeException("Chat not found"));

            // Check access to the chat
            if (!userHasAccessToChat(user, chat)) {
                throw new RuntimeException("You do not have access to this chat");
            }

            // Create and save the message
            ChatMessage message = new ChatMessage();
            message.setChat(chat);
            message.setSender(user);
            message.setContent(messageRequest.content());
            message.setSentAt(LocalDateTime.now());
            message.setRead(false);

            ChatMessage savedMessage = chatMessageRepository.save(message);

            // Update the last message time in the chat
            chat.setUpdatedAt(LocalDateTime.now());
            chatRepository.save(chat);

            ChatMessageDto messageDTO = mapChatMessageToDTO(savedMessage);

            // Get sender information for WebSocket message
            Profile senderProfile = profileRepository.findByUserId(user.getId()).orElse(null);
            String senderName = senderProfile != null ?
                    senderProfile.getFirstName() + " " + senderProfile.getLastName() :
                    "User";

            WebSocketMessageDto webSocketMessage = new WebSocketMessageDto(
                    "MESSAGE",
                    chat.getId(),
                    user.getId(),
                    senderName,
                    messageRequest.content(),
                    savedMessage.getSentAt(),
                    savedMessage.getId()
            );

            // Send to the chat channel
            messagingTemplate.convertAndSend("/topic/chat/" + chat.getId(), webSocketMessage);

            // Send to users' private channels to update chat list
            messagingTemplate.convertAndSendToUser(
                    chat.getStudent().getUser().getEmail(),
                    "/queue/chats",
                    webSocketMessage
            );
            messagingTemplate.convertAndSendToUser(
                    chat.getMentor().getUser().getEmail(),
                    "/queue/chats",
                    webSocketMessage
            );

            return messageDTO;
        }).orElseThrow(() -> new RuntimeException("User not authenticated"));
    }

    @Override
    @Transactional
    public void markMessagesAsRead(Integer chatId) {
        currentUserProvider.getCurrentUser().ifPresent(user -> {
            Chat chat = chatRepository.findById(chatId)
                    .orElseThrow(() -> new RuntimeException("Chat not found"));

            // Check access to the chat
            if (!userHasAccessToChat(user, chat)) {
                throw new RuntimeException("You do not have access to this chat");
            }

            // Find all unread messages from other users
            List<ChatMessage> unreadMessages = chatMessageRepository.findByChatIdOrderBySentAtAsc(chatId)
                    .stream()
                    .filter(msg -> !msg.getSender().getId().equals(user.getId()) && !msg.isRead())
                    .collect(Collectors.toList());

            // Mark them as read
            unreadMessages.forEach(msg -> {
                msg.setRead(true);
                chatMessageRepository.save(msg);
            });

            if (!unreadMessages.isEmpty()) {
                // Send read notification via WebSocket
                WebSocketMessageDto readNotification = new WebSocketMessageDto(
                        "READ",
                        chatId,
                        user.getId(),
                        null,
                        null,
                        LocalDateTime.now(),
                        null
                );

                messagingTemplate.convertAndSend("/topic/chat/" + chatId, readNotification);
            }
        });
    }

    private boolean userHasAccessToChat(User user, Chat chat) {
        if ("ROLE_STUDENT".equals(user.getRole().getRoleName())) {
            return chat.getStudent().getUser().getId().equals(user.getId());
        } else if ("ROLE_MENTOR".equals(user.getRole().getRoleName())) {
            return chat.getMentor().getUser().getId().equals(user.getId());
        }
        return false;
    }

    private ChatDto mapChatToDTO(Chat chat) {
        // Get student and mentor profiles
        Profile studentProfile = profileRepository.findByUserId(chat.getStudent().getUser().getId()).orElse(null);
        Profile mentorProfile = profileRepository.findByUserId(chat.getMentor().getUser().getId()).orElse(null);

        String studentName = studentProfile != null ?
                studentProfile.getFirstName() + " " + studentProfile.getLastName() : "Student";
        String mentorName = mentorProfile != null ?
                mentorProfile.getFirstName() + " " + mentorProfile.getLastName() : "Mentor";

        String studentPictureUrl = studentProfile != null ? studentProfile.getProfilePictureUrl() : "";
        String mentorPictureUrl = mentorProfile != null ? mentorProfile.getProfilePictureUrl() : "";

        // Get the last message
        ChatMessage lastMessage = chat.getMessages().stream()
                .max(Comparator.comparing(ChatMessage::getSentAt))
                .orElse(null);

        LocalDateTime lastMessageTime = lastMessage != null ? lastMessage.getSentAt() : chat.getCreatedAt();
        String lastMessageContent = lastMessage != null ? lastMessage.getContent() : "";

        // Count unread messages for the current user
        long unreadCount = 0;
        User currentUser = currentUserProvider.getCurrentUser().orElse(null);
        if (currentUser != null) {
            unreadCount = chat.getMessages().stream()
                    .filter(msg -> !msg.getSender().getId().equals(currentUser.getId()) && !msg.isRead())
                    .count();
        }

        return new ChatDto(
                chat.getId(),
                chat.getStudent().getId(),
                studentName,
                studentPictureUrl,
                chat.getMentor().getId(),
                mentorName,
                mentorPictureUrl,
                lastMessageTime,
                lastMessageContent,
                unreadCount
        );
    }

    private ChatMessageDto mapChatMessageToDTO(ChatMessage message) {
        Profile senderProfile = profileRepository.findByUserId(message.getSender().getId()).orElse(null);
        String senderName = senderProfile != null ?
                senderProfile.getFirstName() + " " + senderProfile.getLastName() :
                "User";
        String senderProfilePictureUrl = senderProfile != null ?
                senderProfile.getProfilePictureUrl() : "";

        return new ChatMessageDto(
                message.getId(),
                message.getChat().getId(),
                message.getSender().getId(),
                senderName,
                senderProfilePictureUrl,
                message.getContent(),
                message.getSentAt(),
                message.isRead()
        );
    }
}
