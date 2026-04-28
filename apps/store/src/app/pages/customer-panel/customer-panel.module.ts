import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { CustomerPanelComponent } from './customer-panel.component';

const routes: Routes = [
  { path: '', component: CustomerPanelComponent }
];

@NgModule({
  declarations: [CustomerPanelComponent],
  imports: [
    CommonModule,
    RouterModule.forChild(routes)
  ]
})
export class CustomerPanelModule { }
