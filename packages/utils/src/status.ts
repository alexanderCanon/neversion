/**
 * Translates a subscription status to Spanish.
 */
export function getSubscriptionStatusLabel(status: string | undefined): string {
  if (!status) return '';
  const labels: Record<string, string> = {
    ACTIVE: 'Activo',
    EXPIRED: 'Vencido',
    CANCELLED: 'Cancelado',
    SUSPENDED: 'Suspendido',
  };
  return labels[status.toUpperCase()] ?? status;
}

/**
 * Returns the CSS badge status class name for a subscription status.
 */
export function getSubscriptionStatusClass(status: string | undefined): string {
  if (!status) return 'badge-status default';
  switch (status.toUpperCase()) {
    case 'ACTIVE': return 'badge-status active';
    case 'EXPIRED': return 'badge-status expired';
    case 'CANCELLED': return 'badge-status cancelled';
    case 'SUSPENDED': return 'badge-status suspended';
    default: return 'badge-status default';
  }
}

/**
 * Translates an order status to Spanish.
 */
export function getOrderStatusLabel(status: string | undefined): string {
  if (!status) return '';
  const labels: Record<string, string> = {
    PENDING: 'Pendiente',
    VALIDATED: 'Validada',
    COMPLETED: 'Completada',
    REJECTED: 'Rechazada',
    CANCELLED: 'Cancelada'
  };
  return labels[status.toUpperCase()] ?? status;
}

/**
 * Returns the CSS badge status class name for an order status.
 */
export function getOrderStatusClass(status: string | undefined): string {
  if (!status) return 'badge-status default';
  switch (status.toUpperCase()) {
    case 'PENDING': return 'badge-status pending';
    case 'VALIDATED': return 'badge-status validated';
    case 'COMPLETED': return 'badge-status completed';
    case 'REJECTED': return 'badge-status rejected';
    case 'CANCELLED': return 'badge-status cancelled';
    default: return 'badge-status default';
  }
}
