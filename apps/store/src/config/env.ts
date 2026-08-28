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

export const env = {
  apiUrl:
    browserConfig?.apiUrl ||
    import.meta.env.VITE_API_URL ||
    'http://localhost:8080',
  supabaseUrl:
    browserConfig?.supabaseUrl ||
    import.meta.env.VITE_SUPABASE_URL ||
    'https://your-project.supabase.co',
  supabaseAnonKey:
    browserConfig?.supabaseKey ||
    import.meta.env.VITE_SUPABASE_ANON_KEY ||
    'your-anon-key',
  storeVendorUuid:
    browserConfig?.storeVendorUuid ||
    import.meta.env.VITE_STORE_VENDOR_UUID ||
    '00000000-0000-0000-0000-000000000000',
}
