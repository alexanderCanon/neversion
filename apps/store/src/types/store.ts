export type Category = 'streaming' | 'música' | 'tv' | 'gaming'

export interface Platform {
  id: number
  name: string
  desc: string
  category: Category
  perfilPrice: number
  cuentaPrice: number | null
  color: string
  bg: string
  letters: string
  features: string[]
}

export interface RechargePackage {
  id: number
  label: string
  price: number
  badge?: string
}

export interface RechargeGame {
  id: number
  name: string
  color: string
  letters: string
  packages: RechargePackage[]
}

export interface Oferta {
  id: number
  title: string
  desc: string
  originalPrice: number
  salePrice: number
  badge: string
  platforms: string[]
  hot?: boolean
}

export interface Acceso {
  id: number
  platform: string
  status: 'activo' | 'suspendido' | 'por_vencer'
  profile: string
  email: string | null
  password: string | null
  expires: string
  daysLeft: number
  color: string
}

export interface Orden {
  id: string
  date: string
  items: string[]
  total: number
  status: 'completada' | 'procesando' | 'pendiente' | 'cancelada'
}

export interface BankAccount {
  bank: string
  accountNumber: string
  accountType: string
  holder: string
}
