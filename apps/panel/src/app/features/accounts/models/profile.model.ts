export interface ProfileRequest {
  accountId: number;
  name: string;
  pin: string;
  isOwner: boolean;
}

export interface ProfileResponse {
  id: string;
  accountId: number;
  name: string;
  pin: string;
  isOwner: boolean;
  createdAt: string;
}
