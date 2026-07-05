import { Component, OnInit, signal, computed, ViewChild, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { GamesDataService } from '../../services/games-data.service';
import { GameResponse, GameRequest } from '@neversion/models';
import { GamesTableComponent } from '../../components/games-table/games-table.component';
import { GameFormComponent } from '../../components/game-form/game-form.component';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-games-list',
  standalone: true,
  imports: [CommonModule, FormsModule, GamesTableComponent, GameFormComponent],
  templateUrl: './games-list.component.html',
  styleUrl: './games-list.component.scss'
})
export class GamesListComponent implements OnInit {
  @ViewChild(GameFormComponent) gameModal!: GameFormComponent;

  private readonly gamesDataService = inject(GamesDataService);
  private readonly authService = inject(AuthService);

  isSuperAdmin = computed(() => this.authService.userRole() === 'super_admin');

  games = this.gamesDataService.games;
  isLoading = this.gamesDataService.isLoading;

  // Filters
  searchTerm = signal('');
  selectedStatus = signal(''); // '', 'active', 'inactive'

  // Computed state
  filteredGames = computed(() => {
    const term = this.searchTerm().toLowerCase();
    const status = this.selectedStatus();
    
    return this.games().filter(g => {
      const matchName = term ? (g.name?.toLowerCase().includes(term) || g.code?.toLowerCase().includes(term)) : true;
      const matchStatus = status === '' ? true : (status === 'active' ? g.isActive : !g.isActive);
      return matchName && matchStatus;
    });
  });

  ngOnInit(): void {
    this.loadGames();
  }

  loadGames(): void {
    this.gamesDataService.getGames().subscribe();
  }

  onSearchChange(term: string): void {
    this.searchTerm.set(term);
  }

  onStatusChange(status: string): void {
    this.selectedStatus.set(status);
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
}
