import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { GameResponse } from '@neversion/models';

@Component({
  selector: 'app-games-table',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './games-table.component.html',
  styleUrl: './games-table.component.scss'
})
export class GamesTableComponent {
  @Input() games: GameResponse[] = [];
  @Input() isReadOnly = false;
  @Output() editGame = new EventEmitter<GameResponse>();
  @Output() toggleStatus = new EventEmitter<string>();
  @Output() deleteGame = new EventEmitter<string>();

  getGameInitials(name: string): string {
    if (!name) return '';
    const words = name.split(' ');
    if (words.length > 1) {
      return (words[0].charAt(0) + words[1].charAt(0)).toUpperCase();
    }
    return name.substring(0, 2).toUpperCase();
  }
}
