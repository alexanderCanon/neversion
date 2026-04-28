export type OrderStatus = 'PENDING' | 'VALIDATED' | 'COMPLETED' | 'REJECTED' | 'CANCELLED';

export interface OrderResponse {
  id: string;
  reservationId: string;
  status: OrderStatus;
  notes: string | null;
  total: number;
  paymentMethod: string;
  createdAt: string;
}
