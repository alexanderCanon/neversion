import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';
import { ServiceCardComponent } from '../../components/service-card/service-card.component';
import { authGuard } from '../../guards/auth.guard';
import { guestGuard } from '../../guards/guest.guard';
import { CheckoutComponent } from '../checkout/checkout.component';
import { ComboComponent } from '../combo/combo.component';
import { ContactComponent } from '../contact/contact.component';
import { GamesComponent } from '../games/games.component';
import { HomeComponent } from '../home/home.component';
import { HowToBuyComponent } from '../how-to-buy/how-to-buy.component';
import { LoginComponent } from '../login/login.component';
import { OffersComponent } from '../offers/offers.component';
import { PaymentMethodsComponent } from '../payment-methods/payment-methods.component';
import { PaymentPageComponent } from '../payment-page/payment-page.component';
import { PlatformDetailComponent } from '../platform-detail/platform-detail.component';
import { PlatformsComponent } from '../platforms/platforms.component';
import { SupportComponent } from '../support/support.component';
import { WholesalersComponent } from '../wholesalers/wholesalers.component';

const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'login', component: LoginComponent, canActivate: [guestGuard] },
  { path: 'contact', component: ContactComponent },
  { path: 'how-to-buy', component: HowToBuyComponent },
  { path: 'support', component: SupportComponent },
  { path: 'payment-methods', component: PaymentMethodsComponent },
  { path: 'offers', component: OffersComponent },
  { path: 'games', component: GamesComponent },
  { path: 'platforms', component: PlatformsComponent },
  { path: 'platforms/:platformId', component: PlatformDetailComponent },
  { path: 'combo', component: ComboComponent },
  { path: 'wholesalers', component: WholesalersComponent },
  { path: 'checkout', component: CheckoutComponent, canActivate: [authGuard] },
  { path: 'payment-page', component: PaymentPageComponent, canActivate: [authGuard] },
  { path: '**', redirectTo: '', pathMatch: 'full' }
];

@NgModule({
  declarations: [
    ServiceCardComponent,
    HomeComponent,
    LoginComponent,
    ContactComponent,
    HowToBuyComponent,
    SupportComponent,
    PaymentMethodsComponent,
    OffersComponent,
    GamesComponent,
    PlatformsComponent,
    PlatformDetailComponent,
    ComboComponent,
    WholesalersComponent,
    CheckoutComponent,
    PaymentPageComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule.forChild(routes)
  ]
})
export class StorefrontModule {}
