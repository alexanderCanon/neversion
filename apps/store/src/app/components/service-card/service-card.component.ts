import { Component, Input } from '@angular/core';
import { ServiceResponse } from '@neversion/api-client';

@Component({
  selector: 'app-service-card',
  templateUrl: './service-card.component.html',
  styleUrls: ['./service-card.component.css']
})
export class ServiceCardComponent {
  @Input() platforms: ServiceResponse | undefined;
}
