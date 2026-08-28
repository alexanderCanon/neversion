import { createContext, useContext, useEffect, useState, type ReactNode } from 'react'
import type {
  CartItem,
  CartValidationResult,
  DiscountConfig,
  ItemType,
  SpotifyPreference,
} from '../types/cart'

interface CartContextType {
  items: CartItem[]
  isCartOpen: boolean
  openCart: () => void
  closeCart: () => void
  toggleCart: () => void
  addToCart: (
    service: {
      id: string | number
      name: string
      sub?: string
      price: number
      color?: string
      letters?: string
      category?: string
    },
    type?: ItemType
  ) => CartValidationResult
  removeFromCart: (serviceId: string | number, type: ItemType) => void
  updateQuantity: (serviceId: string | number, type: ItemType, quantity: number) => void
  setSpotifyPreference: (serviceId: string | number, preference: SpotifyPreference) => void
  clearCart: () => void
  subtotalProfiles: number
  subtotalFullAccounts: number
  rawTotal: number
  discountPercent: number
  discountAmount: number
  finalTotal: number
  itemCount: number
}

const DEFAULT_DISCOUNT_CONFIG: DiscountConfig = {
  minItems: 2,
  maxItems: 4,
  roundTo: 5,
  tiers: [
    { count: 2, discountPct: 10 },
    { count: 3, discountPct: 15 },
    { count: 4, discountPct: 20 },
    { count: 5, discountPct: 20 },
  ],
}

const STORAGE_KEY = 'neversion_store_cart'

const CartContext = createContext<CartContextType | undefined>(undefined)

export function CartProvider({ children }: { children: ReactNode }) {
  const [items, setItems] = useState<CartItem[]>(() => {
    try {
      const stored = localStorage.getItem(STORAGE_KEY)
      if (stored) {
        const parsed = JSON.parse(stored)
        if (Array.isArray(parsed)) return parsed
      }
    } catch {
      // ignore
    }
    return []
  })

  const [isCartOpen, setIsCartOpen] = useState(false)

  useEffect(() => {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(items))
    } catch {
      // ignore
    }
  }, [items])

  const openCart = () => setIsCartOpen(true)
  const closeCart = () => setIsCartOpen(false)
  const toggleCart = () => setIsCartOpen(v => !v)

  const addToCart = (
    service: {
      id: string | number
      name: string
      sub?: string
      price: number
      color?: string
      letters?: string
      category?: string
    },
    type: ItemType = 'PROFILE'
  ): CartValidationResult => {
    // BR-13 rule: FULL_ACCOUNT must be exclusive
    if (type === 'COMPLETE') {
      if (items.length > 0) {
        return {
          ok: false,
          error: 'FULL_ACCOUNT_EXCLUSIVE',
          message: 'Una cuenta completa no puede combinarse con otros servicios en el mismo pedido.',
        }
      }
      const newItem: CartItem = {
        id: `${service.id}-${type}`,
        serviceId: service.id,
        name: service.name,
        sub: service.sub || 'Cuenta completa',
        category: service.category,
        price: service.price,
        quantity: 1,
        type,
        color: service.color,
        letters: service.letters,
      }
      setItems([newItem])
      setIsCartOpen(true)
      return { ok: true }
    }

    // If there is already a FULL_ACCOUNT
    if (items.some(i => i.type === 'COMPLETE')) {
      return {
        ok: false,
        error: 'FULL_ACCOUNT_EXCLUSIVE',
        message: 'Ya hay una cuenta completa en el carrito. No se pueden agregar más servicios.',
      }
    }

    // Duplicate check
    if (items.some(i => String(i.serviceId) === String(service.id))) {
      return {
        ok: false,
        error: 'DUPLICATE_SERVICE',
        message: `El servicio "${service.name}" ya está en el carrito. Cada servicio solo puede seleccionarse una vez.`,
      }
    }

    // Max profiles check
    const profileCount = items.filter(i => i.type === 'PROFILE').length
    if (profileCount >= DEFAULT_DISCOUNT_CONFIG.maxItems) {
      return {
        ok: false,
        error: 'MAX_PROFILES_EXCEEDED',
        message: `No se pueden agregar más de ${DEFAULT_DISCOUNT_CONFIG.maxItems} perfiles en un solo pedido. Para mayoristas, contáctanos por WhatsApp.`,
      }
    }

    const newItem: CartItem = {
      id: `${service.id}-${type}`,
      serviceId: service.id,
      name: service.name,
      sub: service.sub || 'Perfil individual',
      category: service.category,
      price: service.price,
      quantity: 1,
      type,
      color: service.color,
      letters: service.letters,
    }

    setItems(prev => [...prev, newItem])
    setIsCartOpen(true)
    return { ok: true }
  }

  const removeFromCart = (serviceId: string | number, type: ItemType) => {
    setItems(prev =>
      prev.filter(i => !(String(i.serviceId) === String(serviceId) && i.type === type))
    )
  }

  const updateQuantity = (
    serviceId: string | number,
    type: ItemType,
    quantity: number
  ) => {
    if (quantity <= 0) {
      removeFromCart(serviceId, type)
      return
    }
    setItems(prev =>
      prev.map(i =>
        String(i.serviceId) === String(serviceId) && i.type === type
          ? { ...i, quantity }
          : i
      )
    )
  }

  const setSpotifyPreference = (
    serviceId: string | number,
    preference: SpotifyPreference
  ) => {
    setItems(prev =>
      prev.map(i =>
        String(i.serviceId) === String(serviceId) && i.type === 'PROFILE'
          ? { ...i, spotifyAccountPreference: preference }
          : i
      )
    )
  }

  const clearCart = () => setItems([])

  // Subtotals
  const subtotalProfiles = items
    .filter(i => i.type === 'PROFILE')
    .reduce((acc, i) => acc + i.price * i.quantity, 0)

  const subtotalFullAccounts = items
    .filter(i => i.type === 'COMPLETE')
    .reduce((acc, i) => acc + i.price * i.quantity, 0)

  const rawTotal = subtotalProfiles + subtotalFullAccounts

  // Discount calculation
  const profileItemsCount = items.filter(i => i.type === 'PROFILE').length
  let discountPercent = 0
  if (profileItemsCount >= DEFAULT_DISCOUNT_CONFIG.minItems) {
    const tier = DEFAULT_DISCOUNT_CONFIG.tiers.find(t => t.count === profileItemsCount)
    discountPercent = tier ? tier.discountPct : 0
  }

  let discountAmount = 0
  if (discountPercent > 0) {
    const rawDiscount = (subtotalProfiles * discountPercent) / 100
    const roundTo = DEFAULT_DISCOUNT_CONFIG.roundTo
    discountAmount = roundTo > 0 ? Math.round(rawDiscount / roundTo) * roundTo : rawDiscount
  }

  const finalTotal = Math.max(0, rawTotal - discountAmount)
  const itemCount = items.reduce((acc, i) => acc + i.quantity, 0)

  return (
    <CartContext.Provider
      value={{
        items,
        isCartOpen,
        openCart,
        closeCart,
        toggleCart,
        addToCart,
        removeFromCart,
        updateQuantity,
        setSpotifyPreference,
        clearCart,
        subtotalProfiles,
        subtotalFullAccounts,
        rawTotal,
        discountPercent,
        discountAmount,
        finalTotal,
        itemCount,
      }}
    >
      {children}
    </CartContext.Provider>
  )
}

export function useCart() {
  const context = useContext(CartContext)
  if (!context) {
    throw new Error('useCart must be used within a CartProvider')
  }
  return context
}
