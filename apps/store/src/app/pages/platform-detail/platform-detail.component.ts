import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { PlatformService } from '../../services/platform.service';
import { ImageService } from '../../services/image.service';
import { CartService } from '../../services/cart.service';
import { ToastService } from '../../services/toast.service';
import { ServiceResponse } from '@alexandercanon/api-client-angular';
import { switchMap, finalize } from 'rxjs/operators';

@Component({
  standalone: false,
  selector: 'app-platform-detail',
  templateUrl: './platform-detail.component.html',
  styleUrls: ['./platform-detail.component.css']
})
export class PlatformDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly platformService = inject(PlatformService);
  private readonly imageService = inject(ImageService);
  private readonly cartService = inject(CartService);
  private readonly toastService = inject(ToastService);

  platform?: ServiceResponse;
  loading: boolean = true;
  error: string | null = null;

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const id = params.get('platformId');
      if (id) {
        this.loadPlatform(id);
      } else {
        this.error = 'ID de plataforma no proporcionado';
        this.loading = false;
      }
    });
  }

  private loadPlatform(id: string): void {
    this.loading = true;
    this.error = null;

    this.platformService.getPlatformById(id).subscribe({
      next: (data) => {
        this.platform = data;
        this.loading = false;
      },
      error: (err) => {
        console.warn('Error loading platform by ID, attempting fallback to listActive:', err);
        // Fallback: If getById fails (e.g. 401 Unauthorized), try to find it in the public list
        this.platformService.getPlatforms().subscribe({
          next: (platforms) => {
            const found = platforms.find(p => p.id === id);
            if (found) {
              this.platform = found;
            } else {
              this.error = 'No se encontró el servicio solicitado.';
            }
            this.loading = false;
          },
          error: (fallbackErr) => {
            console.error('Fallback also failed:', fallbackErr);
            this.error = 'No se pudo cargar la información del servicio.';
            this.loading = false;
          }
        });
      }
    });
  }

  get imageUrl(): string {
    return this.imageService.resolveServiceImageUrl(this.platform?.imageUrl);
  }

  addToCart(type: 'PROFILE' | 'COMPLETE'): void {
    if (this.platform) {
      const result = this.cartService.addToCart(this.platform, type);
      if (!result.ok) {
        this.toastService.show(result.message || 'No se pudo agregar al carrito', 'danger', 'Error');
      }
    }
  }

  navigateToCheckout(): void {
    this.router.navigate(['/checkout']);
  }

  goBack(): void {
    this.router.navigate(['/platforms']);
  }
}
