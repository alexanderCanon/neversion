import { Routes } from '@angular/router';
import { LoginComponent } from './features/login/login.component';
import { guestGuard } from './core/guards/guest.guard';
import { authGuard } from './core/guards/auth.guard';

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
                loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent)
            },
            {
                path: 'dashboard/productos/:productId',
                loadComponent: () => import('./features/dashboard/pages/product-accounts-page.component').then(m => m.ProductAccountsPageComponent)
            },
            {
                path: 'services',
                loadComponent: () => import('./features/services/pages/services-list/services-list.component').then(m => m.ServicesListComponent)
            },
            {
                path: 'accounts',
                loadComponent: () => import('./features/accounts/pages/accounts-list/accounts-list.component').then(m => m.AccountsListComponent)
            },
            {
                path: 'clients',
                loadComponent: () => import('./features/clients/pages/clients-list/clients-list.component').then(m => m.ClientsListComponent)
            },
            {
                path: 'subscriptions',
                loadComponent: () => import('./features/subscriptions/pages/subscriptions-list/subscriptions-list.component').then(m => m.SubscriptionsListComponent)
            },
            {
                path: 'reservations',
                loadComponent: () => import('./features/reservations/pages/reservations-list/reservations-list.component').then(m => m.ReservationsListComponent)
            },
            {
                path: 'reservations/:id',
                loadComponent: () => import('./features/reservations/pages/reservation-detail/reservation-detail.component').then(m => m.ReservationDetailComponent)
            },
            {
                path: 'orders',
                loadComponent: () => import('./features/orders/pages/orders-list/orders-list.component').then(m => m.OrdersListComponent)
            }
        ]
    },
    {
        path: '**',
        redirectTo: ''
    }
];
