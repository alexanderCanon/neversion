import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';
import { roleGuard } from './guards/role.guard';

const routes: Routes = [
  {
    path: 'customer-panel',
    loadChildren: () => import('./pages/customer-panel/customer-panel.module').then(m => m.CustomerPanelModule),
    canActivate: [authGuard, roleGuard],
    data: { allowedRoles: ['client'] }
  },
  {
    path: '',
    loadChildren: () => import('./pages/storefront/storefront.module').then(m => m.StorefrontModule)
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {
    initialNavigation: 'enabledBlocking',
    scrollPositionRestoration: 'top'
  })],
  exports: [RouterModule]
})
export class AppRoutingModule { }
