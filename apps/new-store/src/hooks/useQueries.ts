import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  servicesApi,
  vendorsPublicApi,
  gamesApi,
  clientsApi,
  loyaltyPointsApi,
  reservationsApi,
} from '../lib/api'
import { supabase } from '../lib/supabase'
import { env } from '../config/env'
import type {
  ReservationRequest,
  UploadReceiptRequest,
  CreateRenewalReservationRequest,
} from '@alexandercanon/api-client-fetch'

// ─── Public Catalog Queries (Supabase PostgREST Views) ───────────────────────

export interface StoreServiceItem {
  id: string
  uuid: string
  name: string
  category?: string
  description?: string
  imageUrl?: string
  priceProfile?: number
  priceComplete?: number
  durationDays?: number
  maxProfiles?: number
}

export function useServices() {
  return useQuery<StoreServiceItem[]>({
    queryKey: ['services', env.storeVendorUuid],
    queryFn: async () => {
      const { data, error } = await supabase
        .from('v_store_services')
        .select('*')
        .eq('vendor_uuid', env.storeVendorUuid)

      if (error) {
        throw new Error(error.message || 'Error al consultar servicios de la tienda.')
      }

      return (data || []).map(item => ({
        id: item.service_uuid,
        uuid: item.service_uuid,
        name: item.service_name,
        category: item.category,
        description: item.description,
        imageUrl: item.image_url,
        priceProfile: item.price_profile ? Number(item.price_profile) : undefined,
        priceComplete: item.price_full ? Number(item.price_full) : undefined,
        durationDays: item.duration_days,
        maxProfiles: item.max_profiles,
      }))
    },
  })
}

export function useVendorPublic() {
  return useQuery({
    queryKey: ['vendor-public', env.storeVendorUuid],
    queryFn: async () => {
      const { data, error } = await supabase
        .from('v_store_vendors')
        .select('*')
        .eq('vendor_uuid', env.storeVendorUuid)
        .maybeSingle()

      if (error) {
        throw new Error(error.message || 'Error al consultar información del vendedor.')
      }

      if (!data) return null

      return {
        uuid: data.vendor_uuid,
        storeName: data.store_name,
        logoUrl: data.logo_url,
        bankDetails: typeof data.bank_details === 'string' ? data.bank_details : JSON.stringify(data.bank_details),
        discountCfg: typeof data.discount_cfg === 'string' ? data.discount_cfg : JSON.stringify(data.discount_cfg),
      }
    },
  })
}

export interface StoreGameItem {
  id: string
  uuid: string
  name: string
  slug: string
  imageUrl?: string
}

export function useGames() {
  return useQuery<StoreGameItem[]>({
    queryKey: ['games', env.storeVendorUuid],
    queryFn: async () => {
      const { data, error } = await supabase
        .from('v_store_games')
        .select('*')
        .eq('vendor_uuid', env.storeVendorUuid)

      if (error) {
        throw new Error(error.message || 'Error al consultar juegos de la tienda.')
      }

      return (data || []).map(item => ({
        id: item.game_uuid,
        uuid: item.game_uuid,
        name: item.game_name,
        slug: item.game_slug,
        imageUrl: item.image_url,
      }))
    },
  })
}

export interface StoreGameSkuItem {
  id: string
  uuid: string
  code: string
  name: string
  price: number
  imageUrl?: string
}

export function useGameSkus(gameSlug?: string) {
  return useQuery<StoreGameSkuItem[]>({
    queryKey: ['game-skus', env.storeVendorUuid, gameSlug],
    queryFn: async () => {
      if (!gameSlug) return []
      const { data, error } = await supabase
        .from('v_store_game_skus')
        .select('*')
        .eq('vendor_uuid', env.storeVendorUuid)
        .eq('game_slug', gameSlug)

      if (error) {
        throw new Error(error.message || 'Error al consultar paquetes de recarga.')
      }

      return (data || []).map(item => ({
        id: item.sku_uuid,
        uuid: item.sku_uuid,
        code: item.sku_code,
        name: item.sku_name,
        price: Number(item.sku_price),
        imageUrl: item.sku_image_url,
      }))
    },
    enabled: !!gameSlug,
  })
}

// ─── Client Panel Queries (Authenticated) ────────────────────────────────────

export function useMyAccesses() {
  return useQuery({
    queryKey: ['my-accesses'],
    queryFn: async () => {
      return await clientsApi.getMyAccessesClient()
    },
  })
}

export function useMyOrders() {
  return useQuery({
    queryKey: ['my-orders'],
    queryFn: async () => {
      return await clientsApi.getMyOrdersClient()
    },
  })
}

export function useMyPointsSummary() {
  return useQuery({
    queryKey: ['my-points-summary'],
    queryFn: async () => {
      return await loyaltyPointsApi.getMySummaryClientPoints()
    },
  })
}

export function useMyPointsMovements(page = 0, size = 10) {
  return useQuery({
    queryKey: ['my-points-movements', page, size],
    queryFn: async () => {
      return await loyaltyPointsApi.getMyMovementsClientPoints({
        page,
        size,
      })
    },
  })
}

// ─── Reservations Mutations & Queries ────────────────────────────────────────

export function useReservation(id: string | null) {
  return useQuery({
    queryKey: ['reservation', id],
    queryFn: async () => {
      if (!id) return null
      return await reservationsApi.getReservationReservation({ id })
    },
    enabled: !!id,
    refetchInterval: 10000,
  })
}

export function useCreateReservation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (req: ReservationRequest) => {
      return await reservationsApi.createReservationReservation({
        reservationRequest: req,
      })
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['my-orders'] })
    },
  })
}

export function useUploadReceipt() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async ({
      id,
      uploadReceiptRequest,
    }: {
      id: string
      uploadReceiptRequest: UploadReceiptRequest
    }) => {
      return await reservationsApi.uploadReceiptReservation({
        id,
        uploadReceiptRequest,
      })
    },
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['reservation', variables.id] })
    },
  })
}

export function useCreateRenewal() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (req: CreateRenewalReservationRequest) => {
      return await reservationsApi.createRenewalReservationReservation({
        createRenewalReservationRequest: req,
      })
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['my-accesses'] })
    },
  })
}
