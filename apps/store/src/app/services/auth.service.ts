import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, of, throwError } from 'rxjs';
import { delay, map, tap } from 'rxjs/operators';

export interface User {
  id: string;
  email: string;
  role: 'cliente' | 'vendedor' | 'super_admin';
  name?: string;
  lastname?: string;
}

export interface AuthResponse {
  user: User | null;
  token: string | null;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor() {
    // Try to load user from localStorage if exists
    const storedUser = localStorage.getItem('currentUser');
    if (storedUser) {
      this.currentUserSubject.next(JSON.parse(storedUser));
    }
  }

  public get currentUserValue(): User | null {
    return this.currentUserSubject.value;
  }

  login(email: string, password: string): Observable<AuthResponse> {
    // Mocking login logic
    return of({
      user: {
        id: 'mock-id-123',
        email: email,
        role: 'cliente',
        name: 'Cliente',
        lastname: 'Prueba'
      } as User,
      token: 'mock-jwt-token'
    }).pipe(
      delay(1000), // Simulate network delay
      tap(response => {
        if (response.user) {
          localStorage.setItem('currentUser', JSON.stringify(response.user));
          localStorage.setItem('token', response.token!);
          this.currentUserSubject.next(response.user);
        }
      })
    );
  }

  register(userData: any): Observable<AuthResponse> {
    // Mocking registration logic
    return of({
      user: {
        id: 'mock-id-new',
        email: userData.email,
        role: 'cliente',
        name: userData.name,
        lastname: userData.lastname
      } as User,
      token: 'mock-jwt-token-new'
    }).pipe(
      delay(1000),
      tap(response => {
        if (response.user) {
          localStorage.setItem('currentUser', JSON.stringify(response.user));
          localStorage.setItem('token', response.token!);
          this.currentUserSubject.next(response.user);
        }
      })
    );
  }

  logout(): void {
    localStorage.removeItem('currentUser');
    localStorage.removeItem('token');
    this.currentUserSubject.next(null);
  }

  isLoggedIn(): boolean {
    return !!this.currentUserValue;
  }
}
