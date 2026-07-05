import { Component, OnInit, inject } from '@angular/core';
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

  games$!: Observable<GameResponse[]>;

  ngOnInit(): void {
    this.games$ = this.gameService.getGames();
  }

  resolveImageUrl(url?: string): string {
    return this.imageService.resolveServiceImageUrl(url);
  }

  contactToBuy(game: GameResponse): void {
    const text = `Hola, me interesa comprar el juego: *${game.name}* (Código: \`${game.code}\`) por *Q${game.price}*.`;
    const message = encodeURIComponent(text);
    window.open(`https://wa.me/message/WEOAAOMZ5XU3I1?text=${message}`, '_blank');
  }
}
