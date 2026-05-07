import { Routes } from '@angular/router';
import { LoginComponent } from './features/login/login.component';
import { guestGuard } from './core/guards/guest.guard';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
    { 
        path: '', 
        redirectTo: 'dashboard', 
        pathMatch: 'full' 
    },
    { 
        path: 'login', 
        component: LoginComponent,
        canActivate: [guestGuard]
    },
    {
        path: '',
        loadComponent: () => import('./core/layout/main-layout/main-layout.component').then(m => m.MainLayoutComponent),
        canActivate: [authGuard],
        children: [
            {
                path: 'dashboard',
                loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent),
                data: { allowedRoles: ['vendor'] },
                canActivate: [roleGuard]
            },
            {
                path: 'dashboard/productos/:productId',
                loadComponent: () => import('./features/dashboard/pages/product-accounts-page.component').then(m => m.ProductAccountsPageComponent),
                data: { allowedRoles: ['vendor'] },
                canActivate: [roleGuard]
            },
            {
                path: 'services',
                loadComponent: () => import('./features/services/pages/services-list/services-list.component').then(m => m.ServicesListComponent),
                data: { allowedRoles: ['super_admin', 'vendor'] },
                canActivate: [roleGuard]
            },
            {
                path: 'accounts',
                loadComponent: () => import('./features/accounts/pages/accounts-list/accounts-list.component').then(m => m.AccountsListComponent),
                data: { allowedRoles: ['super_admin', 'vendor'] },
                canActivate: [roleGuard]
            },
            {
                path: 'clients',
                loadComponent: () => import('./features/clients/pages/clients-list/clients-list.component').then(m => m.ClientsListComponent),
                data: { allowedRoles: ['super_admin', 'vendor'] },
                canActivate: [roleGuard]
            },
            {
                path: 'clients/:id',
                loadComponent: () => import('./features/clients/pages/client-detail/client-detail.component').then(m => m.ClientDetailComponent),
                data: { allowedRoles: ['super_admin', 'vendor'] },
                canActivate: [roleGuard]
            },
            {
                path: 'subscriptions',
                loadComponent: () => import('./features/subscriptions/pages/subscriptions-list/subscriptions-list.component').then(m => m.SubscriptionsListComponent),
                data: { allowedRoles: ['super_admin', 'vendor'] },
                canActivate: [roleGuard]
            },
            {
                path: 'reservations',
                loadComponent: () => import('./features/reservations/pages/reservations-list/reservations-list.component').then(m => m.ReservationsListComponent),
                data: { allowedRoles: ['super_admin', 'vendor'] },
                canActivate: [roleGuard]
            },
            {
                path: 'reservations/:id',
                loadComponent: () => import('./features/reservations/pages/reservation-detail/reservation-detail.component').then(m => m.ReservationDetailComponent),
                data: { allowedRoles: ['super_admin', 'vendor'] },
                canActivate: [roleGuard]
            },
            {
                path: 'orders',
                loadComponent: () => import('./features/orders/pages/orders-list/orders-list.component').then(m => m.OrdersListComponent),
                data: { allowedRoles: ['super_admin', 'vendor'] },
                canActivate: [roleGuard]
            },
            {
                path: 'orders/:id',
                loadComponent: () => import('./features/orders/pages/order-detail/order-detail.component').then(m => m.OrderDetailComponent),
                data: { allowedRoles: ['super_admin', 'vendor'] },
                canActivate: [roleGuard]
            },
            {
                path: 'vendors',
                loadComponent: () => import('./features/vendors/pages/vendors-list/vendors-list.component').then(m => m.VendorsListComponent),
                data: { allowedRoles: ['super_admin'] },
                canActivate: [roleGuard]
            },
            {
                path: 'vendors/register',
                loadComponent: () => import('./features/vendors/pages/vendor-registration/vendor-registration.component').then(m => m.VendorRegistrationComponent),
                data: { allowedRoles: ['super_admin'] },
                canActivate: [roleGuard]
            }
        ]
    },
    {
        path: '**',
        redirectTo: ''
    }
];
