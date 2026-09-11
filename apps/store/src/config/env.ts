interface NeversionRuntimeConfig {
  apiUrl?: string
  supabaseUrl?: string
  supabaseKey?: string
  storeVendorUuid?: string
}

declare global {
  interface Window {
    __NEVERSION_CONFIG__?: NeversionRuntimeConfig
  }
}

const browserConfig = typeof window !== 'undefined' ? window.__NEVERSION_CONFIG__ : undefined

const serverEnv = typeof process !== 'undefined' ? process.env : {}

export const env = {
  apiUrl:
    browserConfig?.apiUrl ||
    import.meta.env.VITE_API_URL ||
    serverEnv.VITE_API_URL ||
    serverEnv.API_URL ||
    'http://localhost:8080',
  supabaseUrl:
    browserConfig?.supabaseUrl ||
    import.meta.env.VITE_SUPABASE_URL ||
    serverEnv.VITE_SUPABASE_URL ||
    serverEnv.SUPABASE_URL ||
    'https://your-project.supabase.co',
  supabaseAnonKey:
    browserConfig?.supabaseAnonKey ||
    browserConfig?.supabaseKey ||
    import.meta.env.VITE_SUPABASE_ANON_KEY ||
    serverEnv.VITE_SUPABASE_ANON_KEY ||
    serverEnv.SUPABASE_ANON_KEY ||
    'your-anon-key',
  storeVendorUuid:
    browserConfig?.storeVendorUuid ||
    import.meta.env.VITE_STORE_VENDOR_UUID ||
    serverEnv.VITE_STORE_VENDOR_UUID ||
    serverEnv.STORE_VENDOR_UUID ||
    '00000000-0000-0000-0000-000000000000',
}
