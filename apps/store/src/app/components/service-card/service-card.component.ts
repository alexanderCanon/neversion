import { Component, Input } from '@angular/core';
import { Platforms } from '../../model/platforms.model';

@Component({
  selector: 'app-service-card',
  templateUrl: './service-card.component.html',
  styleUrls: ['./service-card.component.css']
})
export class ServiceCardComponent {
  @Input() platforms: Platforms | undefined;
}

