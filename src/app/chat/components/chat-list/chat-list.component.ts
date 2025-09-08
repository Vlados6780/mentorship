import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { ChatService } from '../../services/chat.service';
import { ChatDto } from '../../models/chat.model';
import { ErrorModalComponent } from '../../../shared/components/error-modal/error-modal.component';
import { AuthService } from '../../../auth/services/auth.service';

@Component({
  selector: 'app-chat-list',
  templateUrl: './chat-list.component.html',
  styleUrls: ['./chat-list.component.css'],
  standalone: true,
  imports: [CommonModule, RouterModule, ErrorModalComponent]
})
export class ChatListComponent implements OnInit {
  chats: ChatDto[] = [];
  isLoading = true;
  errorMessage = '';
  showErrorModal = false;
  userRole: string | null = null;

  constructor(
    private chatService: ChatService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/login']);
      return;
    }

    this.userRole = this.authService.getUserRole();
    this.loadChats();
  }

  loadChats(): void {
    this.chatService.getUserChats().subscribe({
      next: (chats) => {
        this.chats = chats;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading chats:', error);
        this.errorMessage = 'Failed to load chat list';
        this.isLoading = false;
        this.showErrorModal = true;
      }
    });
  }

  openChat(chatId: number): void {
    this.router.navigate(['/chat', chatId]);
  }

  closeErrorModal(): void {
    this.showErrorModal = false;
    this.errorMessage = '';
  }

  formatDateTime(dateTime: string): string {
    const date = new Date(dateTime);
    const now = new Date();
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    const yesterday = new Date(today);
    yesterday.setDate(yesterday.getDate() - 1);

    // If message is from today, show only time
    if (date >= today) {
      return date.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
    }

    // If message is from yesterday, show "Yesterday" and time
    if (date >= yesterday && date < today) {
      return `Yesterday, ${date.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' })}`;
    }

    // Otherwise show date and time
    return date.toLocaleDateString('en-US', { day: '2-digit', month: '2-digit' });
  }
}
