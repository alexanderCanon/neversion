import { Component, OnInit, signal, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './main-layout.component.html',
  styleUrl: './main-layout.component.scss'
})
export class MainLayoutComponent implements OnInit {
  private authService = inject(AuthService);
  private router = inject(Router);

  theme = signal<'light' | 'dark'>('light');
  isSuperAdmin = computed(() => this.authService.userRole() === 'super_admin');

  ngOnInit(): void {
    const savedTheme = localStorage.getItem('theme') as 'light' | 'dark' | null;
    const initialTheme = savedTheme || 'light';
    this.theme.set(initialTheme);
    this.applyTheme(initialTheme);
  }

  toggleTheme(): void {
    const newTheme = this.theme() === 'light' ? 'dark' : 'light';
    this.theme.set(newTheme);
    localStorage.setItem('theme', newTheme);
    this.applyTheme(newTheme);
  }

  private applyTheme(theme: 'light' | 'dark'): void {
    document.documentElement.setAttribute('data-bs-theme', theme);
  }

  logout(): void {
    this.authService.signOut().subscribe({
      next: () => {
        this.router.navigate(['/login']);
      },
      error: (err: unknown) => {
        console.error('Logout failed', err);
        // Force redirect anyway
        this.router.navigate(['/login']);
      }
    });
  }
}
