export interface ClientRequest {
  name: string;
  email: string;
  phone?: string;
  notes?: string;
}

export interface ClientResponse {
  id: string;
  name: string;
  email: string;
  phone?: string;
  notes?: string;
  activeSubscriptionCount: number;
  createdAt: string;
}

export interface ClientsFilter {
  name?: string;
  phone?: string;
  email?: string;
}

export interface ActiveSubscriptionSummary {
  id: string;
  serviceName: string;
  profileName: string;
  paymentDueDate: string;
  status: string;
}

export interface OrderSummary {
  id: string;
  status: string;
  createdAt: string;
}

export interface ClientDetail {
  client: ClientResponse;
  activeSubscriptions: ActiveSubscriptionSummary[];
  orderHistory: OrderSummary[];
}
