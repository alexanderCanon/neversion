export interface GameRequest {
  name: string;
  slug: string;
  imageUrl?: string;
}

export interface GameResponse {
  id: string;
  name: string;
  slug: string;
  imageUrl?: string;
  isActive: boolean;
  createdAt: string;
}

export interface GameSkuRequest {
  code: string;
  name: string;
  price: number;
  imageUrl?: string;
  gameUuid?: string;
}

export interface GameSkuResponse {
  id: string;
  code: string;
  name: string;
  price: number;
  imageUrl?: string;
  isActive: boolean;
  gameUuid?: string;
  gameSlug?: string;
  gameName?: string;
  createdAt: string;
}
