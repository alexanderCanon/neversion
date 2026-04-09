export interface ClientRequest {
  name: string;
  phone: string;
  email?: string;
  notes?: string;
}

export interface ClientResponse {
  id: string;
  name: string;
  phone: string;
  email?: string;
  notes?: string;
  createdAt: string;
}

export interface ClientsFilter {
  name?: string;
  phone?: string;
}
