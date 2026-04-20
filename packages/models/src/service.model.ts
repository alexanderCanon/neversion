export interface ServiceDetails {
  description?: string;
  imageUrl?: string;
  category: string;
}

export interface ServiceRequest {
  name: string;
  maxProfiles: number;
  details: ServiceDetails;
}

export interface ServiceResponse {
  id: string;
  name: string;
  maxProfiles: number;
  details: ServiceDetails;
  createdAt: string;
}
