import { Component, OnInit, inject } from '@angular/core';
import { Router } from '@angular/router';
import { GameService } from '../../services/game.service';
import { ImageService } from '../../services/image.service';
import { GameResponse } from '@neversion/api-client';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-games',
  templateUrl: './games.component.html',
  styleUrls: ['./games.component.css']
})
export class GamesComponent implements OnInit {
  private readonly gameService = inject(GameService);
  private readonly imageService = inject(ImageService);
  private readonly router = inject(Router);

  games$!: Observable<GameResponse[]>;

  ngOnInit(): void {
    this.games$ = this.gameService.getGames();
  }

  resolveImageUrl(url?: string): string {
    return this.imageService.resolveServiceImageUrl(url);
  }

  viewGameDetail(game: GameResponse): void {
    if (game.slug) {
      this.router.navigate(['/games', game.slug]);
    }
  }
}
