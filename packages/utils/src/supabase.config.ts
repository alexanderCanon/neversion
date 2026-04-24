import { createClient, SupabaseClient } from '@supabase/supabase-js';

export interface SupabaseConfig {
    url: string;
    key: string;
}

export const createSupabaseClient = (config: SupabaseConfig): SupabaseClient => {
    return createClient(config.url, config.key);
};
