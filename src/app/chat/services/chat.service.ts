import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ChatDto, ChatMessageDto, MessageRequest, NewChatRequest } from '../models/chat.model';

@Injectable({
  providedIn: 'root'
})
export class ChatService {
  private apiUrl = 'http://localhost:8080/api/chat';

  constructor(private http: HttpClient) { }

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
  }

  getUserChats(): Observable<ChatDto[]> {
    return this.http.get<ChatDto[]>(`${this.apiUrl}/list`, {
      headers: this.getAuthHeaders()
    });
  }

  createChat(mentorId: number): Observable<ChatDto> {
    const request: NewChatRequest = { mentorId };
    return this.http.post<ChatDto>(`${this.apiUrl}/create`, request, {
      headers: this.getAuthHeaders()
    });
  }

  getChatMessages(chatId: number): Observable<ChatMessageDto[]> {
    return this.http.get<ChatMessageDto[]>(`${this.apiUrl}/${chatId}/messages`, {
      headers: this.getAuthHeaders()
    });
  }

  sendMessage(chatId: number, content: string): Observable<ChatMessageDto> {
    const request: MessageRequest = { chatId, content };
    return this.http.post<ChatMessageDto>(`${this.apiUrl}/send`, request, {
      headers: this.getAuthHeaders()
    });
  }

  markMessagesAsRead(chatId: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${chatId}/read`, {}, {
      headers: this.getAuthHeaders()
    });
  }
}
