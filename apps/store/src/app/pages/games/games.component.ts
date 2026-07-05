import { Component, OnInit, inject } from '@angular/core';
import { GameService } from '../../services/game.service';
import { ImageService } from '../../services/image.service';
import { GameResponse } from '@neversion/api-client';
import { Observable, map } from 'rxjs';

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
    this.games$ = this.gameService.getGames().pipe(
      map(games => [...games].sort((a, b) => (a.price || 0) - (b.price || 0)))
    );
  }

  resolveImageUrl(url?: string): string {
    return this.imageService.resolveServiceImageUrl(url);
  }

  contactToBuy(game: GameResponse): void {
    const text = `Quiero comprar este producto: *${game.name}* (Código: \`${game.code}\`). porfavor necesito las cuentas de banco`;
    const message = encodeURIComponent(text);
    window.open(`https://wa.me/message/WEOAAOMZ5XU3I1?text=${message}`, '_blank');
  }
}
