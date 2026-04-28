import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HomeComponent } from './pages/home/home.component';
import { PaymentMethodsComponent } from './pages/payment-methods/payment-methods.component';
import { OffersComponent } from './pages/offers/offers.component';
import { GamesComponent } from './pages/games/games.component';
import { PlatformsComponent } from './pages/platforms/platforms.component';
import { ComboComponent } from './pages/combo/combo.component';
import { WholesalersComponent } from './pages/wholesalers/wholesalers.component';
import { CheckoutComponent } from './pages/checkout/checkout.component';
import { AdminComponent } from './pages/admin/admin.component';
import { PlatformDetailComponent } from './pages/platform-detail/platform-detail.component';
import { ContactComponent } from './pages/contact/contact.component';
import { LoginComponent } from './pages/login/login.component';
import { authGuard } from './guards/auth.guard';
import { guestGuard } from './guards/guest.guard';
import { roleGuard } from './guards/role.guard';

const routes: Routes = [
  { path: '', component: HomeComponent },
  { 
    path: 'login', 
    component: LoginComponent,
    canActivate: [guestGuard]
  },
  { path: 'contact', component: ContactComponent },
  { path: 'payment-methods', component: PaymentMethodsComponent },
  { path: 'offers', component: OffersComponent },
  { path: 'games', component: GamesComponent },
  { path: 'platforms', component: PlatformsComponent },
  { path: 'combo', component: ComboComponent },
  { path: 'wholesalers', component: WholesalersComponent },
  { 
    path: 'checkout', 
    component: CheckoutComponent,
    canActivate: [authGuard]
  },
  { 
    path: 'admin', 
    component: AdminComponent,
    canActivate: [authGuard, roleGuard],
    data: { allowedRoles: ['super_admin'] }
  },
  { 
    path: 'customer-panel', 
    loadChildren: () => import('./pages/customer-panel/customer-panel.module').then(m => m.CustomerPanelModule),
    canActivate: [authGuard, roleGuard],
    data: { allowedRoles: ['cliente'] }
  },
  { path: 'platforms/:platformId', component: PlatformDetailComponent },
  { path: '**', redirectTo: '', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {
    initialNavigation: 'enabledBlocking'
  })],
  exports: [RouterModule]
})
export class AppRoutingModule { }
