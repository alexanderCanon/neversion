import { Component, OnInit, inject } from '@angular/core';
import { PlatformService } from '../../services/platform.service';
import { CartService } from '../../services/cart.service';
import { Observable } from 'rxjs';
import { ServiceResponse } from '@neversion/api-client';

@Component({
  selector: 'app-platforms',
  templateUrl: './platforms.component.html',
  styleUrls: ['./platforms.component.css'],
})
export class PlatformsComponent implements OnInit {

  private readonly _platformService = inject(PlatformService);
  private readonly _cartService = inject(CartService);

  platforms$!: Observable<ServiceResponse[]>;

  ngOnInit(): void {
    this.platforms$ = this._platformService.getPlatforms();
  }

  addToCart(service: ServiceResponse, type: 'PROFILE' | 'COMPLETE'): void {
    this._cartService.addToCart(service, type);
  }
}
