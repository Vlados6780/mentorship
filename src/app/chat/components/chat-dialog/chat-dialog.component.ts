import { Component, OnInit, OnDestroy, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ChatService } from '../../services/chat.service';
import { AuthService } from '../../../auth/services/auth.service';
import { ChatMessageDto, ChatDto } from '../../models/chat.model';
import { ErrorModalComponent } from '../../../shared/components/error-modal/error-modal.component';
import { Subscription, interval } from 'rxjs';

@Component({
  selector: 'app-chat-dialog',
  templateUrl: './chat-dialog.component.html',
  styleUrls: ['./chat-dialog.component.css'],
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, ErrorModalComponent]
})
export class ChatDialogComponent implements OnInit, OnDestroy, AfterViewChecked {
  @ViewChild('messagesContainer') messagesContainer?: ElementRef;

  chatId: number | null = null;
  mentorId: number | null = null;
  isNewChat = false;
  messages: ChatMessageDto[] = [];
  chatInfo: ChatDto | null = null;
  messageForm: FormGroup;
  isLoading = true;
  errorMessage = '';
  showErrorModal = false;
  currentUserId: number | null = null;
  pollingSubscription?: Subscription;

  constructor(
    private route: ActivatedRoute,
    public router: Router,
    private fb: FormBuilder,
    private chatService: ChatService,
    public authService: AuthService,
  ) {
    this.messageForm = this.fb.group({
      message: ['', [Validators.required, Validators.maxLength(1000)]]
    });
  }

  ngOnInit(): void {
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/login']);
      return;
    }

    const userRole = this.authService.getUserRole();
    if (userRole === 'ROLE_MENTOR') {
      this.extractUserIdFromToken();
    }

    this.route.paramMap.subscribe(params => {
      // Check if it's a new chat or an existing one
      if (this.router.url.includes('/chat/new/')) {
        this.isNewChat = true;
        this.mentorId = Number(params.get('id'));

        // Check if the current user is a mentor
        if (userRole === 'ROLE_MENTOR') {
          this.errorMessage = 'Mentors cannot create chats with other mentors';
          this.showErrorModal = true;
          this.isLoading = false;
          return;
        }

        // Create a new chat
        this.createNewChat();
      } else {
        this.chatId = Number(params.get('id'));
        this.loadChatMessages();
        this.startPolling();
      }
    });
  }

  ngAfterViewChecked(): void {
    this.scrollToBottom();
  }

  ngOnDestroy(): void {
    if (this.pollingSubscription) {
      this.pollingSubscription.unsubscribe();
    }
  }

  private extractUserIdFromToken(): void {
    const token = localStorage.getItem('token');
    if (token) {
      try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        // In this case, we take the email and try to extract the ID (this is a simplification,
        // in reality, the ID should be obtained from another source)
        this.currentUserId = parseInt(payload.sub.split('@')[0], 10);
      } catch (e) {
        console.error('Error decoding token', e);
      }
    }
  }

  private createNewChat(): void {
    if (!this.mentorId) return;

    this.chatService.createChat(this.mentorId).subscribe({
      next: (chat) => {
        this.chatId = chat.chatId;
        this.chatInfo = chat;
        this.isLoading = false;
        this.isNewChat = false;

        // Update the URL to the existing chat URL
        this.router.navigate(['/chat', chat.chatId], { replaceUrl: true });
        this.startPolling();
      },
      error: (error) => {
        console.error('Error creating chat:', error);
        this.errorMessage = 'Failed to create chat with mentor';
        this.isLoading = false;
        this.showErrorModal = true;
      }
    });
  }

  private loadChatMessages(): void {
    if (!this.chatId) return;

    this.chatService.getChatMessages(this.chatId).subscribe({
      next: (messages) => {
        this.messages = messages;
        this.isLoading = false;
        this.markMessagesAsRead();
      },
      error: (error) => {
        console.error('Error loading messages:', error);
        this.errorMessage = 'Failed to load messages';
        this.isLoading = false;
        this.showErrorModal = true;
      }
    });
  }

  private markMessagesAsRead(): void {
    if (!this.chatId) return;

    this.chatService.markMessagesAsRead(this.chatId).subscribe({
      error: (error) => {
        console.error('Error marking messages as read:', error);
      }
    });
  }

  private startPolling(): void {
    // Poll the server every 5 seconds for new messages
    this.pollingSubscription = interval(5000).subscribe(() => {
      if (this.chatId) {
        this.refreshMessages();
      }
    });
  }

  private refreshMessages(): void {
    if (!this.chatId) return;

    this.chatService.getChatMessages(this.chatId).subscribe({
      next: (messages) => {
        if (messages.length > this.messages.length) {
          const wasAtBottom = this.isScrolledToBottom();
          this.messages = messages;
          this.markMessagesAsRead();

          // Scroll to bottom only if the user was already at the bottom
          if (wasAtBottom) {
            setTimeout(() => this.scrollToBottom(), 100);
          }
        }
      },
      error: (error) => {
        console.error('Error refreshing messages:', error);
      }
    });
  }

  private isScrolledToBottom(): boolean {
    if (!this.messagesContainer) return true;

    const element = this.messagesContainer.nativeElement;
    return Math.abs(element.scrollHeight - element.clientHeight - element.scrollTop) < 50;
  }

  private scrollToBottom(): void {
    if (this.messagesContainer) {
      const element = this.messagesContainer.nativeElement;
      element.scrollTop = element.scrollHeight;
    }
  }

  sendMessage(): void {
    if (this.messageForm.invalid || !this.chatId) return;

    const content = this.messageForm.get('message')?.value;

    this.chatService.sendMessage(this.chatId, content).subscribe({
      next: (message) => {
        this.messages.push(message);
        this.messageForm.reset();
        setTimeout(() => this.scrollToBottom(), 100);
      },
      error: (error) => {
        console.error('Error sending message:', error);
        this.errorMessage = 'Failed to send message';
        this.showErrorModal = true;
      }
    });
  }

  handleKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter') {
      event.preventDefault();
      if (event.ctrlKey) {
        const currentValue = this.messageForm.get('message')?.value || '';
        this.messageForm.get('message')?.setValue(currentValue + '\n');
      } else {
        this.sendMessage();
      }
    }
  }

  closeErrorModal(): void {
    this.showErrorModal = false;
    this.errorMessage = '';

    // If the error is related to a mentor trying to create a chat with another mentor,
    // redirect to the mentors list page
    if (this.errorMessage.includes('Mentors cannot create chats')) {
      this.router.navigate(['/mentors']);
    }
  }

  formatMessageTime(dateTime: string): string {
    const date = new Date(dateTime);
    return date.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
  }

  isOwnMessage(senderId: number): boolean {
    if (this.currentUserId) {
      return senderId === this.currentUserId;
    }

    // If the user ID is not defined, use another method to determine ownership
    const token = localStorage.getItem('token');
    if (token) {
      try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        const email = payload.sub;
        // Simple assumption that senderId may be encoded in the email
        return email.includes(senderId.toString());
      } catch (e) {
        console.error('Error determining message ownership', e);
        return false;
      }
    }
    return false;
  }
}
