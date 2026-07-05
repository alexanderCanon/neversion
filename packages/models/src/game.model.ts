export interface GameRequest {
  code: string;
  name: string;
  price: number;
  imageUrl?: string;
}

export interface GameResponse {
  id: string;
  code: string;
  name: string;
  price: number;
  imageUrl?: string;
  isActive: boolean;
  createdAt: string;
}
