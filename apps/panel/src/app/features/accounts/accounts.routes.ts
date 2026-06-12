import { Routes } from '@angular/router';

export const accountsRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/service-selector/service-selector.component').then(
        (m) => m.ServiceSelectorComponent
      ),
  },
  {
    path: ':serviceUuid',
    loadComponent: () =>
      import('./pages/accounts-list/accounts-list.component').then(
        (m) => m.AccountsListComponent
      ),
  },
];
