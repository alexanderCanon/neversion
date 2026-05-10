import { Component, Input, inject } from '@angular/core';
import { ServiceResponse } from '@neversion/api-client';
import { ImageService } from '../../services/image.service';

@Component({
  selector: 'app-service-card',
  templateUrl: './service-card.component.html',
  styleUrls: ['./service-card.component.css']
})
export class ServiceCardComponent {
  private readonly imageService = inject(ImageService);
  @Input() platforms: ServiceResponse | undefined;

  get imageUrl(): string {
    return this.imageService.resolveServiceImageUrl(this.platforms?.imageUrl);
  }
}
