import { environment } from '../../../environments/environment';

interface NeversionRuntimeConfig {
  apiUrl?: string;
  supabaseUrl?: string;
  supabaseKey?: string;
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
  apiUrl: browserConfig?.apiUrl ?? environment.apiUrl,
  supabaseUrl: browserConfig?.supabaseUrl ?? environment.supabaseUrl,
  supabaseKey: browserConfig?.supabaseKey ?? environment.supabaseKey
};
