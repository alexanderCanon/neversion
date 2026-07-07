import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { GameSkuResponse } from '@neversion/models';

@Component({
  selector: 'app-game-skus-table',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './game-skus-table.component.html',
  styleUrl: './game-skus-table.component.scss'
})
export class GameSkusTableComponent {
  @Input() skus: GameSkuResponse[] = [];
  @Input() isReadOnly = false;
  @Output() editSku = new EventEmitter<GameSkuResponse>();
  @Output() toggleStatus = new EventEmitter<string>();
  @Output() deleteSku = new EventEmitter<string>();

  getSkuInitials(name: string): string {
    if (!name) return '';
    const words = name.split(' ');
    if (words.length > 1) {
      return (words[0].charAt(0) + words[1].charAt(0)).toUpperCase();
    }
    return name.substring(0, 2).toUpperCase();
  }
}
