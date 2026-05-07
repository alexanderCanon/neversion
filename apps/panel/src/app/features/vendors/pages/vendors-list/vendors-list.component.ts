import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-vendors-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="container mt-4">
      <div class="d-flex justify-content-between align-items-center mb-4">
        <h2>Vendedores</h2>
        <a routerLink="register" class="btn btn-primary">Registrar Nuevo Vendedor</a>
      </div>
      
      <div class="alert alert-info">
        Próximamente: Lista de vendedores registrados.
      </div>
    </div>
  `
})
export class VendorsListComponent {}
