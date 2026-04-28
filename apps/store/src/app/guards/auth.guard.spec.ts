import { TestBed } from '@angular/core/testing';
import { Router, RouterStateSnapshot, ActivatedRouteSnapshot } from '@angular/router';
import { authGuard } from './auth.guard';
import { AuthService } from '../services/auth.service';

describe('authGuard (Store)', () => {
  let routerMock: jasmine.SpyObj<Router>;
  let authServiceMock: jasmine.SpyObj<AuthService>;

  beforeEach(() => {
    routerMock = jasmine.createSpyObj('Router', ['createUrlTree']);
    authServiceMock = jasmine.createSpyObj('AuthService', ['isLoggedIn']);

    TestBed.configureTestingModule({
      providers: [
        { provide: Router, useValue: routerMock },
        { provide: AuthService, useValue: authServiceMock }
      ]
    });
  });

  const runGuard = (route: any, state: any) => {
    return TestBed.runInInjectionContext(() => authGuard(route, state));
  };

  it('should return true if the user is logged in', () => {
    authServiceMock.isLoggedIn.and.returnValue(true);

    const mockRoute = {} as ActivatedRouteSnapshot;
    const mockState = { url: '/protected-route' } as RouterStateSnapshot;

    const result = runGuard(mockRoute, mockState);

    expect(result).toBeTrue();
    expect(authServiceMock.isLoggedIn).toHaveBeenCalled();
    expect(routerMock.createUrlTree).not.toHaveBeenCalled();
  });

  it('should return a UrlTree directing to /login if the user is not logged in', () => {
    authServiceMock.isLoggedIn.and.returnValue(false);

    const mockUrlTree = {} as any;
    routerMock.createUrlTree.and.returnValue(mockUrlTree);

    const mockRoute = {} as ActivatedRouteSnapshot;
    const mockState = { url: '/protected-route' } as RouterStateSnapshot;

    const result = runGuard(mockRoute, mockState);

    expect(result).toBe(mockUrlTree);
    expect(authServiceMock.isLoggedIn).toHaveBeenCalled();
    expect(routerMock.createUrlTree).toHaveBeenCalledWith(['/login'], { queryParams: { returnUrl: '/protected-route' } });
  });
});
