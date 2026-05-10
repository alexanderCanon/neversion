import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-vendors-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="container-fluid py-4 h-100 bg-surface">
      <div class="d-flex flex-column flex-sm-row justify-content-between align-items-sm-center gap-3 mb-4">
        <div>
            <h3 class="mb-1 fw-bold text-dark">Directorio de Vendedores</h3>
            <p class="text-muted small mb-0">Administración de socios comerciales y permisos de plataforma.</p>
        </div>
        <a routerLink="register" class="btn btn-primary d-flex align-items-center px-4 py-2">
            <i class="bi bi-person-plus-fill me-2"></i> Nuevo Vendedor
        </a>
      </div>
      
      <div class="empty-state text-center py-5 bg-white rounded-4 shadow-sm border mt-4">
          <i class="bi bi-shop text-muted display-4 mb-3"></i>
          <h4 class="fw-semibold">Módulo en Desarrollo</h4>
          <p class="text-muted">Próximamente: Lista detallada de vendedores registrados y sus métricas de desempeño.</p>
          <div class="d-flex justify-content-center gap-2 mt-3">
              <div class="spinner-grow spinner-grow-sm text-primary" role="status"></div>
              <div class="spinner-grow spinner-grow-sm text-primary" style="animation-delay: 0.1s" role="status"></div>
              <div class="spinner-grow spinner-grow-sm text-primary" style="animation-delay: 0.2s" role="status"></div>
          </div>
      </div>
    </div>
  `,
  styles: [`
    .bg-surface { background-color: #f8fafc; }
  `]
})
export class VendorsListComponent {}
