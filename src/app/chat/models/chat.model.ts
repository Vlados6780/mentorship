export interface ChatDto {
  chatId: number;
  studentId: number;
  studentName: string;
  studentPictureUrl: string;
  mentorId: number;
  mentorName: string;
  mentorPictureUrl: string;
  lastMessageTime: string;
  lastMessageContent: string;
  unreadCount: number;
}

export interface ChatMessageDto {
  messageId: number;
  chatId: number;
  senderId: number;
  senderName: string;
  senderProfilePictureUrl: string;
  content: string;
  sentAt: string;
  read: boolean;
}

export interface MessageRequest {
  chatId: number;
  content: string;
}

export interface NewChatRequest {
  mentorId: number;
}

export interface WebSocketMessageDto {
  type: string; // MESSAGE, TYPING, READ
  chatId: number;
  senderId: number;
  senderName: string;
  content: string;
  sentAt: string;
  messageId: number;
}
