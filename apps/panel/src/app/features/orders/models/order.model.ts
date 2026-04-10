export type OrderStatus = 'PENDING' | 'COMPLETED' | 'REJECTED' | 'CANCELLED';

export interface OrderResponse {
  id: string;
  reservationId: string;
  status: OrderStatus;
  notes: string | null;
  createdAt: string;
}
