import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';
import { CustomerPanelComponent } from './customer-panel.component';
import { AccessesComponent } from './components/accesses/accesses.component';
import { OrdersComponent } from './components/orders/orders.component';
import { ReceiptsComponent } from './components/receipts/receipts.component';
import { ProfileComponent } from './components/profile/profile.component';
import { PointsComponent } from './components/points/points.component';

const routes: Routes = [
  { path: '', component: CustomerPanelComponent }
];

@NgModule({
  declarations: [
    CustomerPanelComponent,
    AccessesComponent,
    OrdersComponent,
    ReceiptsComponent,
    ProfileComponent,
    PointsComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule.forChild(routes)
  ]
})
export class CustomerPanelModule { }
