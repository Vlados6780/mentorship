// src/app/app.routes.ts
import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login', loadComponent: () => import('./auth/components/login/login.component').then(m => m.LoginComponent) },
  { path: 'register', loadComponent: () => import('./auth/components/register/register.component').then(m => m.RegisterComponent) },
  { path: 'create-profile', loadComponent: () => import('./profile/components/profile-create/profile-create.component').then(m => m.ProfileCreateComponent) },
  { path: 'verify-email', loadComponent: () => import('./auth/components/email-verification/email-verification.component').then(m => m.EmailVerificationComponent) },
  { path: 'profile', loadComponent: () => import('./profile/components/profile-view/profile-view.component').then(m => m.ProfileViewComponent) },
  { path: 'mentors', loadComponent: () => import('./mentorship/components/mentor-list/mentor-list.component').then(m => m.MentorListComponent) },
  { path: 'chats', loadComponent: () => import('./chat/components/chat-list/chat-list.component').then(m => m.ChatListComponent) },
  { path: 'chat/:id', loadComponent: () => import('./chat/components/chat-dialog/chat-dialog.component').then(m => m.ChatDialogComponent) },
  { path: 'chat/new/:id', loadComponent: () => import('./chat/components/chat-dialog/chat-dialog.component').then(m => m.ChatDialogComponent) },
];
