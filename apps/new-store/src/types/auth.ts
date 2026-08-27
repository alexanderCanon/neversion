export type UserRole = 'client' | 'admin' | 'superadmin'

export interface User {
  id: string
  email: string
  role: UserRole
  name?: string
  lastname?: string
  phone?: string
}

export interface RegisterFormData {
  name: string
  lastname: string
  email: string
  password: string
  phone: string
  checkNewsletter?: boolean
  checkCookies?: boolean
}

export interface PendingOAuthUser {
  supabaseUid: string
  email: string
  name: string
}

export interface AuthResult {
  success: boolean
  user: User | null
  error: string | null
}
