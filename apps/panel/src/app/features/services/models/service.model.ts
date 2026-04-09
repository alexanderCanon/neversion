export interface ServiceRequest {
  name: string;
  maxProfiles: number;
  details: any;
}

export interface ServiceResponse {
  id: string;
  name: string;
  maxProfiles: number;
  details: any;
  createdAt: string;
}
