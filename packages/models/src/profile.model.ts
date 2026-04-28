export enum ProfileStatus {
  AVAILABLE = 'AVAILABLE',
  ACTIVE = 'ACTIVE',
  RESERVED = 'RESERVED',
  OCCUPIED = 'OCCUPIED',
  BLOCKED = 'BLOCKED',
  EXPIRED = 'EXPIRED',
}

export interface ProfileRequest {
  accountId: string; // UUID
  name?: string;
  pin?: string;
  isOwner?: boolean;
}

export interface ProfileResponse {
  id: string; // UUID
  accountId: string;
  name: string;
  pin?: string;
  isOwner: boolean;
  status: ProfileStatus;
  createdAt: string;
}

export interface ChangeProfileStatusRequest {
  status: ProfileStatus;
}
