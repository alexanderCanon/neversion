import { Routes } from '@angular/router';

export const subscriptionsRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/subscriptions-list/subscriptions-list.component').then(
        (m) => m.SubscriptionsListComponent
      ),
  },
  {
    path: ':id',
    loadComponent: () =>
      import('./pages/subscription-detail/subscription-detail.component').then(
        (m) => m.SubscriptionDetailComponent
      ),
  },
];
