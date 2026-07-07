import { Component, OnInit, ViewChild, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { GamesDataService } from '../../services/games-data.service';
import { GameResponse, GameRequest } from '@neversion/models';
import { GameFormComponent } from '../../components/game-form/game-form.component';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-game-selector',
  standalone: true,
  imports: [CommonModule, GameFormComponent],
  templateUrl: './game-selector.component.html',
  styleUrl: './game-selector.component.scss'
})
export class GameSelectorComponent implements OnInit {
  @ViewChild(GameFormComponent) gameModal!: GameFormComponent;

  private readonly gamesDataService = inject(GamesDataService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  isSuperAdmin = computed(() => this.authService.userRole() === 'super_admin');

  games = this.gamesDataService.games;
  isLoading = this.gamesDataService.isLoading;

  ngOnInit(): void {
    this.loadGames();
  }

  loadGames(): void {
    this.gamesDataService.getGames().subscribe();
  }

  selectGame(game: GameResponse): void {
    this.router.navigate(['/games', game.id]);
  }

  openNewGameModal(): void {
    if (this.gameModal) {
      this.gameModal.openModal();
    }
  }

  onEditGame(game: GameResponse): void {
    if (this.gameModal) {
      this.gameModal.openModal(game);
    }
  }

  onToggleStatus(id: string): void {
    this.gamesDataService.toggleGameStatus(id).subscribe();
  }

  onDeleteGame(id: string): void {
    if (confirm('¿Estás seguro de que deseas eliminar este juego? (Se desactivará lógicamente)')) {
      this.gamesDataService.deleteGame(id).subscribe({
        next: () => this.loadGames(),
        error: (err) => console.error('Failed to delete game', err)
      });
    }
  }

  onSaveGame(request: GameRequest): void {
    const editId = this.gameModal.editingGameId;

    if (editId) {
      this.gamesDataService.updateGame(editId, request).subscribe({
        next: () => this.loadGames(),
        error: (err) => console.error('Failed to update game', err)
      });
    } else {
      this.gamesDataService.createGame(request).subscribe({
        next: () => this.loadGames(),
        error: (err) => console.error('Failed to create game', err)
      });
    }
  }

  getGameInitials(name: string): string {
    if (!name) return '';
    const words = name.split(' ');
    if (words.length > 1) {
      return (words[0].charAt(0) + words[1].charAt(0)).toUpperCase();
    }
    return name.substring(0, 2).toUpperCase();
  }
}
