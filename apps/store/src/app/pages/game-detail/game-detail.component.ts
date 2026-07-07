import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { GameService } from '../../services/game.service';
import { ImageService } from '../../services/image.service';
import { GameResponse, GameSkuResponse } from '@neversion/api-client';

@Component({
  standalone: false,
  selector: 'app-game-detail',
  templateUrl: './game-detail.component.html',
  styleUrls: ['./game-detail.component.css']
})
export class GameDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly gameService = inject(GameService);
  private readonly imageService = inject(ImageService);

  game?: GameResponse;
  skus: GameSkuResponse[] = [];
  loading = true;
  error: string | null = null;

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const slug = params.get('slug');
      if (slug) {
        this.loadGame(slug);
      } else {
        this.error = 'Slug de juego no proporcionado';
        this.loading = false;
      }
    });
  }

  private loadGame(slug: string): void {
    this.loading = true;
    this.error = null;

    this.gameService.getGameBySlug(slug).subscribe({
      next: (game) => {
        this.game = game;
        this.loadSkus(slug);
      },
      error: (err) => {
        console.error('Error loading game by slug:', err);
        this.error = 'No se encontró el juego solicitado.';
        this.loading = false;
      }
    });
  }

  private loadSkus(slug: string): void {
    this.gameService.getGameSkusBySlug(slug).subscribe({
      next: (skus) => {
        // Sort by price ascending for display
        this.skus = [...skus].sort((a, b) => (a.price || 0) - (b.price || 0));
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading SKUs:', err);
        this.loading = false;
      }
    });
  }

  resolveImageUrl(url?: string): string {
    return this.imageService.resolveServiceImageUrl(url);
  }

  contactToBuy(sku: GameSkuResponse): void {
    const gameName = this.game?.name || 'Juego';
    const text = `Quiero comprar: *${sku.name}* (${gameName}) - Q${sku.price}. Por favor necesito la información de pago.`;
    const message = encodeURIComponent(text);
    window.open(`https://wa.me/message/WEOAAOMZ5XU3I1?text=${message}`, '_blank');
  }

  goBack(): void {
    this.router.navigate(['/games']);
  }
}
