import { Component, inject } from '@angular/core';
import { ToastService, Toast } from './services/toast.service';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  private readonly toastService = inject(ToastService);
  
  title = 'neversion-site';
  toasts$: Observable<Toast[]> = this.toastService.toasts$;

  removeToast(toast: Toast): void {
    this.toastService.remove(toast);
  }
}
