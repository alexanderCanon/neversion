export interface ServiceRequest {
  name: string;
  category: 'STREAMING' | 'SOFTWARE' | 'GIFT_CARD' | 'RECHARGE' | 'DIGITAL_SERVICE';
  priceProfile: number;
  priceFull: number;
  durationDays: number;
  maxProfiles: number;
  description?: string;
  imageUrl?: string;
  details?: string;
}

export interface ServiceResponse {
  id: string;
  name: string;
  category: 'STREAMING' | 'SOFTWARE' | 'GIFT_CARD' | 'RECHARGE' | 'DIGITAL_SERVICE';
  priceProfile: number;
  priceFull: number;
  durationDays: number;
  maxProfiles: number;
  isActive: boolean;
  description?: string;
  imageUrl?: string;
  details?: string;
  createdAt: string;
}

export interface ServicesFilter {
  category?: string;
  isActive?: boolean;
}
