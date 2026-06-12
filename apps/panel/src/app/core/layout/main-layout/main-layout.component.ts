import { Component, signal, computed, inject } from '@angular/core';
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
export class MainLayoutComponent {
  private authService = inject(AuthService);
  private router = inject(Router);


  isRecoveringContext = signal(false);
  isSuperAdmin = computed(() => this.authService.userRole() === 'super_admin');
  contextLoadFailed = this.authService.contextLoadFailed;

  userInitials = computed(() => {
    const user = this.authService.currentUser();
    const name = user?.user_metadata?.['name'] || '';
    const lastname = user?.user_metadata?.['lastname'] || '';
    
    if (name || lastname) {
      const first = name ? name.charAt(0).toUpperCase() : '';
      const last = lastname ? lastname.charAt(0).toUpperCase() : '';
      return `${first}${last}`.trim() || 'US';
    }
    
    const context = this.authService.currentContext();
    if (context?.storeName) {
      const parts = context.storeName.split(' ');
      if (parts.length > 1) {
        return `${parts[0].charAt(0)}${parts[1].charAt(0)}`.toUpperCase();
      }
      return context.storeName.substring(0, 2).toUpperCase();
    }
    
    if (user?.email) {
      return user.email.substring(0, 2).toUpperCase();
    }
    
    return 'US';
  });



  retryContext(): void {
    this.isRecoveringContext.set(true);
    this.authService.retryCurrentContext()
      .finally(() => this.isRecoveringContext.set(false));
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
