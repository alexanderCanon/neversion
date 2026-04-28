import { TestBed } from '@angular/core/testing';
import { AuthService } from './auth.service';
import { SupabaseService } from './supabase.service';
import { AuthApiService } from '@neversion/api-client';
import { of, throwError } from 'rxjs';

describe('AuthService (Store)', () => {
  let service: AuthService;
  let supabaseServiceMock: jasmine.SpyObj<any>;
  let authApiServiceMock: jasmine.SpyObj<AuthApiService>;

  beforeEach(() => {
    supabaseServiceMock = {
      client: {
        auth: jasmine.createSpyObj('auth', [
          'signInWithPassword',
          'signUp',
          'signOut',
          'getSession',
          'onAuthStateChange'
        ])
      }
    };

    // Setup default mock returns for the constructor calls
    supabaseServiceMock.client.auth.getSession.and.returnValue(Promise.resolve({ data: { session: null } }));
    supabaseServiceMock.client.auth.onAuthStateChange.and.returnValue({ data: { subscription: { unsubscribe: () => {} } } });

    authApiServiceMock = jasmine.createSpyObj('AuthApiService', ['registerClient']);

    TestBed.configureTestingModule({
      providers: [
        AuthService,
        { provide: SupabaseService, useValue: supabaseServiceMock },
        { provide: AuthApiService, useValue: authApiServiceMock }
      ]
    });

    service = TestBed.inject(AuthService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('Login', () => {
    it('should handle successful login', (done) => {
      const mockResponse = { data: { user: { id: '1', user_metadata: { role: 'cliente' } } }, error: null };
      supabaseServiceMock.client.auth.signInWithPassword.and.returnValue(Promise.resolve(mockResponse));

      service.login('test@test.com', 'pass').subscribe((res) => {
        expect(res.success).toBeTrue();
        expect(res.user?.id).toBe('1');
        expect(service.currentUserValue?.id).toBe('1');
        expect(service.isLoggedIn()).toBeTrue();
        done();
      });
    });

    it('should handle login error', (done) => {
      const mockResponse = { data: null, error: { message: 'Invalid credentials' } };
      supabaseServiceMock.client.auth.signInWithPassword.and.returnValue(Promise.resolve(mockResponse));

      service.login('test@test.com', 'wrong').subscribe((res) => {
        expect(res.success).toBeFalse();
        expect(res.error).toBe('Invalid credentials');
        expect(service.currentUserValue).toBeNull();
        expect(service.isLoggedIn()).toBeFalse();
        done();
      });
    });
  });

  describe('Register', () => {
    it('should handle successful registration', (done) => {
      const mockSupaResponse = { data: { user: { id: '1' } }, error: null };
      supabaseServiceMock.client.auth.signUp.and.returnValue(Promise.resolve(mockSupaResponse));
      authApiServiceMock.registerClient.and.returnValue(of({} as any));

      const userData = { email: 'new@test.com', password: 'pass', name: 'N', lastname: 'L', phone: '123' };
      service.register(userData).subscribe((res) => {
        expect(res.success).toBeTrue();
        expect(authApiServiceMock.registerClient).toHaveBeenCalled();
        done();
      });
    });

    it('should handle supabase registration error', (done) => {
      const mockSupaResponse = { data: null, error: { message: 'Email taken' } };
      supabaseServiceMock.client.auth.signUp.and.returnValue(Promise.resolve(mockSupaResponse));

      const userData = { email: 'taken@test.com', password: 'pass', name: 'N', lastname: 'L', phone: '123' };
      service.register(userData).subscribe((res) => {
        expect(res.success).toBeFalse();
        expect(res.error).toBe('Email taken');
        expect(authApiServiceMock.registerClient).not.toHaveBeenCalled();
        done();
      });
    });
  });

  describe('Logout', () => {
    it('should sign out and clear user state', async () => {
      supabaseServiceMock.client.auth.signOut.and.returnValue(Promise.resolve());

      // Force user state to simulate being logged in
      (service as any).currentUserSubject.next({ id: '1' });
      expect(service.isLoggedIn()).toBeTrue();

      service.logout();

      // Allow promise to resolve
      await new Promise(resolve => setTimeout(resolve, 0));

      expect(supabaseServiceMock.client.auth.signOut).toHaveBeenCalled();
      expect(service.currentUserValue).toBeNull();
      expect(service.isLoggedIn()).toBeFalse();
    });
  });

  describe('State Observables', () => {
    it('should emit loading state correctly during login', (done) => {
      const mockResponse = { data: { user: { id: '1' } }, error: null };
      supabaseServiceMock.client.auth.signInWithPassword.and.returnValue(Promise.resolve(mockResponse));

      let loadingStates: boolean[] = [];
      service.isLoading$.subscribe(state => loadingStates.push(state));

      service.login('test@test.com', 'pass').subscribe(() => {
        // Initial false, then true when starting, then false when finished
        expect(loadingStates).toEqual([false, true, false]);
        done();
      });
    });
  });
});
