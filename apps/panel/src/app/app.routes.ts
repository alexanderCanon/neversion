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
                data: { allowedRoles: ['vendor'] },
                canActivate: [roleGuard]
            },
            {
                path: 'games',
                loadChildren: () => import('./features/games/games.routes').then(m => m.gamesRoutes),
                data: { allowedRoles: ['vendor'] },
                canActivate: [roleGuard]
            },
            {
                path: 'accounts',
                loadChildren: () => import('./features/accounts/accounts.routes').then(m => m.accountsRoutes),
                data: { allowedRoles: ['vendor'] },
                canActivate: [roleGuard]
            },
            {
                path: 'clients',
                loadComponent: () => import('./features/clients/pages/clients-list/clients-list.component').then(m => m.ClientsListComponent),
                data: { allowedRoles: ['vendor'] },
                canActivate: [roleGuard]
            },
            {
                path: 'clients/:id',
                loadComponent: () => import('./features/clients/pages/client-detail/client-detail.component').then(m => m.ClientDetailComponent),
                data: { allowedRoles: ['vendor'] },
                canActivate: [roleGuard]
            },
            {
                path: 'subscriptions',
                loadChildren: () => import('./features/subscriptions/subscriptions.routes').then(m => m.subscriptionsRoutes),
                data: { allowedRoles: ['vendor'] },
                canActivate: [roleGuard]
            },
            {
                path: 'reservations',
                loadComponent: () => import('./features/reservations/pages/reservations-list/reservations-list.component').then(m => m.ReservationsListComponent),
                data: { allowedRoles: ['vendor'] },
                canActivate: [roleGuard]
            },
            {
                path: 'reservations/:id',
                loadComponent: () => import('./features/reservations/pages/reservation-detail/reservation-detail.component').then(m => m.ReservationDetailComponent),
                data: { allowedRoles: ['vendor'] },
                canActivate: [roleGuard]
            },
            {
                path: 'orders',
                loadComponent: () => import('./features/orders/pages/orders-list/orders-list.component').then(m => m.OrdersListComponent),
                data: { allowedRoles: ['vendor'] },
                canActivate: [roleGuard]
            },
            {
                path: 'orders/:id',
                loadComponent: () => import('./features/orders/pages/order-detail/order-detail.component').then(m => m.OrderDetailComponent),
                data: { allowedRoles: ['vendor'] },
                canActivate: [roleGuard]
            },
            {
                path: 'monitoring',
                loadComponent: () => import('./features/monitoring/monitoring.component').then(m => m.MonitoringComponent),
                data: { allowedRoles: ['super_admin'] },
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
            },
            {
                path: 'discount-config',
                loadComponent: () => import('./features/vendors/pages/discount-config/discount-config.component').then(m => m.DiscountConfigComponent),
                data: { allowedRoles: ['vendor'] },
                canActivate: [roleGuard]
            },
            {
                path: 'rewards-config',
                loadComponent: () => import('./features/vendors/pages/rewards-config/rewards-config.component').then(m => m.RewardsConfigComponent),
                data: { allowedRoles: ['vendor'] },
                canActivate: [roleGuard]
            },
            {
                path: 'help',
                loadComponent: () => import('./features/help/help.component').then(m => m.HelpComponent),
                data: { allowedRoles: ['vendor', 'super_admin'] },
                canActivate: [roleGuard]
            },
            {
                path: 'settings',
                loadComponent: () => import('./features/settings/settings.component').then(m => m.SettingsComponent),
                data: { allowedRoles: ['vendor', 'super_admin'] },
                canActivate: [roleGuard]
            }
        ]
    },
    {
        path: '**',
        redirectTo: ''
    }
];
