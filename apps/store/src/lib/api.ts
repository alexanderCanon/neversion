import {
  Configuration,
  AuthApi,
  ServicesApi,
  GamesApi,
  GameSKUsApi,
  ReservationsApi,
  OrdersApi,
  ProfilesApi,
  LoyaltyPointsApi,
  VendorsPublicApi,
  VendorsApi,
  ClientsApi,
} from '@alexandercanon/api-client-fetch'
import { env } from '../config/env'
import { supabase } from './supabase'

export const apiConfig = new Configuration({
  basePath: env.apiUrl,
  accessToken: async () => {
    const { data: { session } } = await supabase.auth.getSession()
    return session?.access_token || ''
  },
})

export const authApi = new AuthApi(apiConfig)
export const servicesApi = new ServicesApi(apiConfig)
export const gamesApi = new GamesApi(apiConfig)
export const gameSKUsApi = new GameSKUsApi(apiConfig)
export const reservationsApi = new ReservationsApi(apiConfig)
export const ordersApi = new OrdersApi(apiConfig)
export const profilesApi = new ProfilesApi(apiConfig)
export const loyaltyPointsApi = new LoyaltyPointsApi(apiConfig)
export const vendorsPublicApi = new VendorsPublicApi(apiConfig)
export const vendorsApi = new VendorsApi(apiConfig)
export const clientsApi = new ClientsApi(apiConfig)
