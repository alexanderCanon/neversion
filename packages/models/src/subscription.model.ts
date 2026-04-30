export enum SubscriptionStatus {
  ACTIVE = 'ACTIVE',
  EXPIRED = 'EXPIRED',
  CANCELLED = 'CANCELLED',
  SUSPENDED = 'SUSPENDED',
}

export interface CreateSubscriptionRequest {
  profileId: string; // UUID
  clientId: string; // UUID
  paymentDueDate: string;
  price: number;
  notes?: string;
}

export interface SubscriptionResponse {
  id: string;
  profileId: string;
  clientId: string;
  paymentDueDate: string;
  price: number;
  purchaseDate: string;
  notes?: string;
  status: SubscriptionStatus;
}

// Kept if there's a view joining everything, otherwise you might need to fetch them separatedly
export interface SubscriptionDashboardDTO {
  id?: string;
  purchaseDate?: string;
  paymentDueDate?: string;
  status?: string;
  email?: string; // from Client?
  profileName?: string; // from Profile
  serviceName?: string; // from Service
  price?: number;
}

export interface SubscriptionsFilter {
  status?: SubscriptionStatus;
  clientId?: string;
  profileId?: string;
  serviceId?: string;
}
