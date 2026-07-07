import { Routes } from '@angular/router';

export const gamesRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/game-selector/game-selector.component').then(
        (m) => m.GameSelectorComponent
      ),
  },
  {
    path: ':gameUuid',
    loadComponent: () =>
      import('./pages/skus-list/skus-list.component').then(
        (m) => m.SkusListComponent
      ),
  },
];
