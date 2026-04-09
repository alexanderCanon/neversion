export interface ProductSummary {
  productId: string;
  productName: string;
  category: string;
  totalAccounts: number;
}

export interface AccountGroup {
  accountId: string;
  email: string;
  password: string;
  cutOffDate: string;
  accountType: 'FAMILY' | 'INDIVIDUAL';
  accountStatus: 'AVAILABLE' | 'ASSIGNED' | 'EXPIRED';
  maxProfiles: number;
  occupiedProfiles: number;
  availableProfiles: number;
  availability: 'PARTIAL' | 'NO_AVAILABILITY' | 'INDIVIDUAL' | 'COMPLETE';
}

export interface ProfileItem {
  profileId: string;
  profileName: string | null;
  pin: string | null;
  profileStatus: 'AVAILABLE' | 'OCCUPIED' | 'BLOCKED';
  subscription: ProfileSubscription | null;
}

export interface ProfileSubscription {
  subscriptionId: string;
  startDate: string;
  endDate: string;
  status: 'ACTIVE' | 'EXPIRED' | 'EXPIRING_SOON' | 'CANCELLED' | 'SUSPENDED';
  customer: ProfileCustomer;
}

export interface ProfileCustomer {
  id: string;
  name: string;
  phone: string;
  type: 'CLIENT' | 'PROFILE';
}

export interface DashboardMetrics {
  totalAccounts: number;
  availableProfiles: number;
  occupiedProfiles: number;
  activeSubscriptions: number;
  expiringSoon: number;
}
