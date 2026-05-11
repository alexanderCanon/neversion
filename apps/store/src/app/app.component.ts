import { Component, OnInit, inject } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';
import { ToastService, Toast } from './services/toast.service';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  private readonly router = inject(Router);
  private readonly toastService = inject(ToastService);
  
  title = 'neversion-site';
  toasts$: Observable<Toast[]> = this.toastService.toasts$;

  ngOnInit(): void {
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe(() => {
      window.scrollTo(0, 0);
    });
  }

  removeToast(toast: Toast): void {
    this.toastService.remove(toast);
  }
}
