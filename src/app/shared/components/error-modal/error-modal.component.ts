import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-error-modal',
  templateUrl: './error-modal.component.html',
  styleUrls: ['./error-modal.component.css'],
  standalone: true,
  imports: [CommonModule]
})
export class ErrorModalComponent {
  @Input() message: string = '';
  @Output() close = new EventEmitter<void>();

  onClose(): void {
    this.close.emit();
  }
}
