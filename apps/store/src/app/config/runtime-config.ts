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

const browserConfig = window.__NEVERSION_CONFIG__;

export const runtimeConfig = {
  apiUrl: browserConfig?.apiUrl
    ?? 'http://localhost:8080',
  supabaseUrl: browserConfig?.supabaseUrl
    ?? 'https://your-project.supabase.co',
  supabaseKey: browserConfig?.supabaseKey
    ?? 'your-anon-key',
  storeVendorUuid: browserConfig?.storeVendorUuid
    ?? '00000000-0000-0000-0000-000000000000'
};
