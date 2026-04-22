import { ProfileResponse } from './profile.model';
import { ServiceResponse } from './service.model';

export enum SaleMode {
  BY_PROFILE = 'BY_PROFILE',
  FULL_ACCOUNT = 'FULL_ACCOUNT',
}

export enum AccountStatus {
  AVAILABLE = 'AVAILABLE',
  ASSIGNED = 'ASSIGNED',
  EXPIRED = 'EXPIRED',
}

export interface AccountRequest {
  email: string;
  password: string;
  serviceId: number;
  saleMode: SaleMode;
  renewalDate: string;
  notes?: string;
  plan?: string;
}

export interface AccountResponse {
  id: string; // UUID
  email: string;
  password: string;
  serviceId: number;
  service?: ServiceResponse;
  saleMode: SaleMode;
  renewalDate: string;
  notes?: string;
  plan?: string;
  maxProfiles: number;
  activeProfiles: number;
  profiles: ProfileResponse[];
  createdAt: string;
  status?: string; // Kept for frontend convenience if backend still sends it/can be inferred
}

export interface AccountsFilter {
  serviceId?: number;
  saleMode?: SaleMode;
  isActive?: boolean; // You can still filter locally or adjust based on your API
}
