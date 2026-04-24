import { Injectable } from '@angular/core';
import { Router, CanActivate } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable({
  providedIn: 'root'
})
export class GuestGuard implements CanActivate {
  constructor(
    private router: Router,
    private authService: AuthService
  ) {}

  canActivate() {
    if (!this.authService.isLoggedIn()) {
      return true;
    }

    // if logged in, redirect to home
    this.router.navigate(['/']);
    return false;
  }
}
