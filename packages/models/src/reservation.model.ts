export type ReservationStatus = 'PENDING' | 'UPLOADED' | 'VALIDATED' | 'EXPIRED' | 'CANCELLED';

export interface ReservationItemRequest {
  inventoryId: number;
  qty: number;
}

export interface CreateReservationRequest {
  clientId?: string;
  items: ReservationItemRequest[];
}

export interface ReservationDetailResponse {
  id: string;
  inventoryId: number;
  qty: number;
  unitPrice: number;
  subtotal: number;
}

export interface ReservationResponse {
  id: string;
  clientId: string | null;
  status: ReservationStatus;
  discount: number;
  total: number;
  receiptUrl: string | null;
  expirationDate: string;
  createdAt: string;
  details: ReservationDetailResponse[];
}

export interface ReservationsFilter {
  status?: ReservationStatus;
}
