export type ItemType = 'PROFILE' | 'COMPLETE'
export type SpotifyPreference = 'CUENTA_NUEVA' | 'CUENTA_PROPIA'

export interface CartItem {
  id: string
  serviceId: string | number
  name: string
  sub?: string
  category?: string
  price: number
  quantity: number
  type: ItemType
  color?: string
  letters?: string
  spotifyAccountPreference?: SpotifyPreference
}

export type CartValidationError =
  | 'DUPLICATE_SERVICE'
  | 'MAX_PROFILES_EXCEEDED'
  | 'FULL_ACCOUNT_EXCLUSIVE'

export interface CartValidationResult {
  ok: boolean
  error?: CartValidationError
  message?: string
}

export interface DiscountTier {
  count: number
  discountPct: number
}

export interface DiscountConfig {
  minItems: number
  maxItems: number
  roundTo: number
  tiers: DiscountTier[]
}
