import { Component, OnInit, ViewChild, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { GameSkusDataService } from '../../services/game-skus-data.service';
import { GamesDataService } from '../../services/games-data.service';
import { GameSkuResponse, GameSkuRequest, GameResponse } from '@neversion/models';
import { GameSkusTableComponent } from '../../components/game-skus-table/game-skus-table.component';
import { GameSkuFormComponent } from '../../components/gamesku-form/gamesku-form.component';

@Component({
  selector: 'app-skus-list',
  standalone: true,
  imports: [CommonModule, FormsModule, GameSkusTableComponent, GameSkuFormComponent],
  templateUrl: './skus-list.component.html',
  styleUrl: './skus-list.component.scss'
})
export class SkusListComponent implements OnInit {
  @ViewChild(GameSkuFormComponent) skuModal!: GameSkuFormComponent;

  private readonly gameSkusDataService = inject(GameSkusDataService);
  private readonly gamesDataService = inject(GamesDataService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly currentGame = signal<GameResponse | null>(null);
  readonly gameUuidParam = signal<string | null>(null);

  skus = this.gameSkusDataService.gameSkus;
  isLoading = this.gameSkusDataService.isLoading;

  // Available games for the SKU form selector
  readonly availableGames = signal<GameResponse[]>([]);

  // Filters
  searchTerm = signal('');
  selectedStatus = signal('');

  // Computed state
  filteredSkus = computed(() => {
    const term = this.searchTerm().toLowerCase();
    const status = this.selectedStatus();

    return this.skus().filter(s => {
      const matchName = term ? (s.name?.toLowerCase().includes(term) || s.code?.toLowerCase().includes(term)) : true;
      const matchStatus = status === '' ? true : (status === 'active' ? s.isActive : !s.isActive);
      return matchName && matchStatus;
    });
  });

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      const gameUuid = params['gameUuid'];
      if (gameUuid) {
        this.gameUuidParam.set(gameUuid);
        this.loadGame(gameUuid);
        this.loadSkus(gameUuid);
        this.loadAvailableGames();
      }
    });
  }

  private loadGame(gameUuid: string): void {
    this.gamesDataService.getGameById(gameUuid).subscribe({
      next: (game) => this.currentGame.set(game),
      error: (err) => {
        console.error('Failed to load game', err);
        this.router.navigate(['/games']);
      }
    });
  }

  private loadSkus(gameUuid: string): void {
    this.gameSkusDataService.getGameSkus({ gameUuid }).subscribe();
  }

  private loadAvailableGames(): void {
    // Load all games for the selector in the SKU form
    this.gamesDataService.getGames().subscribe({
      next: () => this.availableGames.set(this.gamesDataService.games())
    });
  }

  reloadSkus(): void {
    const gameUuid = this.gameUuidParam();
    if (gameUuid) {
      this.loadSkus(gameUuid);
    }
  }

  onSearchChange(term: string): void {
    this.searchTerm.set(term);
  }

  onStatusChange(status: string): void {
    this.selectedStatus.set(status);
  }

  goBackToSelector(): void {
    this.router.navigate(['/games']);
  }

  openNewSkuModal(): void {
    if (this.skuModal) {
      this.skuModal.openModal();
    }
  }

  onEditSku(sku: GameSkuResponse): void {
    if (this.skuModal) {
      this.skuModal.openModal(sku);
    }
  }

  onToggleStatus(id: string): void {
    this.gameSkusDataService.toggleGameSkuStatus(id).subscribe();
  }

  onDeleteSku(id: string): void {
    if (confirm('¿Estás seguro de que deseas eliminar esta recarga? (Se desactivará lógicamente)')) {
      this.gameSkusDataService.deleteGameSku(id).subscribe({
        next: () => this.reloadSkus(),
        error: (err) => console.error('Failed to delete SKU', err)
      });
    }
  }

  onSaveSku(request: GameSkuRequest): void {
    const editId = this.skuModal.editingSkuId;

    if (editId) {
      this.gameSkusDataService.updateGameSku(editId, request).subscribe({
        next: () => this.reloadSkus(),
        error: (err) => console.error('Failed to update SKU', err)
      });
    } else {
      this.gameSkusDataService.createGameSku(request).subscribe({
        next: () => this.reloadSkus(),
        error: (err) => console.error('Failed to create SKU', err)
      });
    }
  }
}
