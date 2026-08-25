interface NeversionRuntimeConfig {
  apiUrl?: string;
  supabaseUrl?: string;
  supabaseKey?: string;
  grafanaUrl?: string;
}

declare global {
  interface Window {
    __NEVERSION_CONFIG__?: NeversionRuntimeConfig;
  }
}

const browserConfig = typeof window !== 'undefined'
  ? window.__NEVERSION_CONFIG__
  : undefined;

export const runtimeConfig = {
  apiUrl: browserConfig?.apiUrl ?? 'http://localhost:8080',
  supabaseUrl: browserConfig?.supabaseUrl ?? 'https://your-project.supabase.co',
  supabaseKey: browserConfig?.supabaseKey ?? 'your-anon-key',
  grafanaUrl: browserConfig?.grafanaUrl ?? ''
};
