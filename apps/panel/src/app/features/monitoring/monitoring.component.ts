import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { runtimeConfig } from '../../core/config/runtime-config';

@Component({
  selector: 'app-monitoring',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './monitoring.component.html',
  styleUrl: './monitoring.component.scss'
})
export class MonitoringComponent {
  readonly grafanaUrl = runtimeConfig.grafanaUrl?.trim() ?? '';
  readonly hasGrafanaUrl = this.grafanaUrl.length > 0;
}
