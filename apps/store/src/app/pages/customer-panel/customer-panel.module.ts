import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { CustomerPanelComponent } from './customer-panel.component';
import { AccessesComponent } from './components/accesses/accesses.component';

const routes: Routes = [
  { path: '', component: CustomerPanelComponent }
];

@NgModule({
  declarations: [
    CustomerPanelComponent,
    AccessesComponent
  ],
  imports: [
    CommonModule,
    RouterModule.forChild(routes)
  ]
})
export class CustomerPanelModule { }
