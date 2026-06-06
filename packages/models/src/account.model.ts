import { ProfileResponse } from './profile.model';
import { ServiceResponse } from './service.model';

export enum SaleMode {
  BY_PROFILE = 'BY_PROFILE',
  FULL_ACCOUNT = 'FULL_ACCOUNT',
}

export enum AccountStatus {
  AVAILABLE = 'AVAILABLE',
  PARTIAL = 'PARTIAL',
  FULL = 'FULL',
  EXPIRED = 'EXPIRED',
}

export interface AccountRequest {
  email: string;
  password?: string; // Maps to 'pass' in API
  serviceId: string; // UUID
  saleMode: SaleMode;
  renewalDate: string;
  cost: number;
  plan?: string;
  source?: string;
  purchasedAt?: string;
  notes?: string;
}

export interface AccountResponse {
  id: string; // UUID
  email: string;
  password?: string; // Optional because API might not return it in list/detail
  serviceId: string;
  serviceUuid?: string;
  serviceName?: string;
  service?: ServiceResponse;
  saleMode: SaleMode;
  status: AccountStatus;
  renewalDate: string;
  cost: number;
  plan?: string;
  source?: string;
  purchasedAt?: string;
  notes?: string;
  createdAt: string;
  
  // Stats
  totalProfiles: number;
  availableProfiles: number;
  occupiedProfiles: number;
  blockedProfiles: number;
  
  // Legacy / UI helpers
  profiles: ProfileResponse[];
}

export interface AccountsFilter {
  serviceId?: string;
  status?: AccountStatus;
}
