import { Component } from '@angular/core';

@Component({
  selector: 'app-customer-panel',
  template: `
    <div class="container py-5">
      <div class="row">
        <div class="col-12">
          <h2 class="mb-4 fw-bold">Panel de Cliente</h2>
          <!-- You can add tabs here for other sections like Orders later -->
          <ul class="nav nav-tabs">
            <li class="nav-item">
              <a class="nav-link active" href="javascript:void(0)">Accesos</a>
            </li>
          </ul>

          <app-customer-accesses></app-customer-accesses>
        </div>
      </div>
    </div>
  `,
})
export class CustomerPanelComponent {}
