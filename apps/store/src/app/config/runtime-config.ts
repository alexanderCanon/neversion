interface NeversionRuntimeConfig {
  apiUrl?: string;
  supabaseUrl?: string;
  supabaseKey?: string;
  storeVendorUuid?: string;
}

declare global {
  interface Window {
    __NEVERSION_CONFIG__?: NeversionRuntimeConfig;
  }
}

type ProcessLike = {
  env?: Record<string, string | undefined>;
};

const browserConfig = typeof window !== 'undefined'
  ? window.__NEVERSION_CONFIG__
  : undefined;

const serverEnv = typeof process !== 'undefined'
  ? (process as ProcessLike).env
  : undefined;

export const runtimeConfig = {
  apiUrl: browserConfig?.apiUrl
    ?? serverEnv?.['STORE_API_URL']
    ?? 'http://localhost:8080/api/v1',
  supabaseUrl: browserConfig?.supabaseUrl
    ?? serverEnv?.['STORE_SUPABASE_URL']
    ?? 'https://your-project.supabase.co',
  supabaseKey: browserConfig?.supabaseKey
    ?? serverEnv?.['STORE_SUPABASE_KEY']
    ?? 'your-anon-key',
  storeVendorUuid: browserConfig?.storeVendorUuid
    ?? serverEnv?.['STORE_VENDOR_UUID']
    ?? '00000000-0000-0000-0000-000000000000'
};
