import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { ToolbarComponent } from './components/toolbar/toolbar.component';
import { FooterComponent } from './components/footer/footer.component';
import { HomeComponent } from './pages/home/home.component';
import { ServiceCardComponent } from './components/service-card/service-card.component';
import { FloatingWhatsappComponent } from './components/floating-whatsapp/floating-whatsapp.component';
import { PaymentMethodsComponent } from './pages/payment-methods/payment-methods.component';
import { OffersComponent } from './pages/offers/offers.component';
import { ComboComponent } from './pages/combo/combo.component';
import { PlatformsComponent } from './pages/platforms/platforms.component';
import { GamesComponent } from './pages/games/games.component';
import { WholesalersComponent } from './pages/wholesalers/wholesalers.component';
import { AdminComponent } from './pages/admin/admin.component';
import { CheckoutComponent } from './pages/checkout/checkout.component';
import { PaymentPageComponent } from './pages/payment-page/payment-page.component';
import { PlatformDetailComponent } from './pages/platform-detail/platform-detail.component';
import { ContactComponent } from './pages/contact/contact.component';
import { LoginComponent } from './pages/login/login.component';

@NgModule({
  declarations: [
    AppComponent,
    ToolbarComponent,
    FooterComponent,
    HomeComponent,
    ServiceCardComponent,
    FloatingWhatsappComponent,
    PaymentMethodsComponent,
    OffersComponent,
    ComboComponent,
    PlatformsComponent,
    GamesComponent,
    WholesalersComponent,
    AdminComponent,
    CheckoutComponent,
    PaymentPageComponent,
    PlatformDetailComponent,
    ContactComponent,
    LoginComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    ReactiveFormsModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
