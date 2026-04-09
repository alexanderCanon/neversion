import { Routes } from '@angular/router';

export const accountsRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/accounts-list/accounts-list.component').then(
        (m) => m.AccountsListComponent
      ),
  },
];
