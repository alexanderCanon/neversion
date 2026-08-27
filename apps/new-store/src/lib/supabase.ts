import { createClient, type SupabaseClient } from '@supabase/supabase-js'
import { env } from '../config/env'

let supabaseInstance: SupabaseClient | null = null

export function getSupabase(): SupabaseClient {
  if (!supabaseInstance) {
    supabaseInstance = createClient(env.supabaseUrl, env.supabaseAnonKey)
  }
  return supabaseInstance
}

export const supabase = getSupabase()
