import { Component, OnInit, inject } from '@angular/core';
import { PlatformService } from '../../services/platform.service';
import { ImageService } from '../../services/image.service';
import { CartService } from '../../services/cart.service';
import { ServiceResponse } from '@neversion/api-client';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Component({
  selector: 'app-games',
  templateUrl: './games.component.html',
  styleUrls: ['./games.component.css']
})
export class GamesComponent implements OnInit {
  private readonly platformService = inject(PlatformService);
  private readonly imageService = inject(ImageService);
  private readonly cartService = inject(CartService);

  games$!: Observable<ServiceResponse[]>;

  ngOnInit(): void {
    this.games$ = this.platformService.getPlatforms().pipe(
      map(services => services.filter(s => 
        s.category === 'GIFT_CARD' || s.category === 'SOFTWARE'
      ))
    );
  }

  resolveImageUrl(url?: string): string {
    return this.imageService.resolveServiceImageUrl(url);
  }

  addToCart(service: ServiceResponse, type: 'PROFILE' | 'COMPLETE'): void {
    this.cartService.addToCart(service, type);
  }
}
