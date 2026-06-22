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
  /** Invitation link or personal email for Spotify Family slots. */
  notes?: string;
  isOwner?: boolean;
}

export interface ProfileResponse {
  id: string; // UUID
  accountId: string;
  name: string;
  pin?: string;
  /** Invitation link or personal email for Spotify Family slots. */
  notes?: string;
  isOwner: boolean;
  status: ProfileStatus;
  createdAt: string;
}

export interface ChangeProfileStatusRequest {
  status: ProfileStatus;
}
