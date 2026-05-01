export type UserRole = 'super_admin' | 'vendor' | 'client';

export interface User {
    id: string;
    email: string;
    role: UserRole;
    name?: string;
    lastname?: string;
    phone?: string;
    createdAt?: string;
}

export interface RegisterVendorRequest {
    email: string;
    password?: string; // Optional if backend generates it
    name: string;
    lastname: string;
    phone: string;
    storeName: string; // Specific for US-012
}

export interface AuthResult {
    success: boolean;
    user: User | null;
    error: string | null;
}
