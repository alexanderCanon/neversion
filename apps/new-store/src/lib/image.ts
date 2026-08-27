import { env } from '../config/env'

const STORAGE_BUCKET = 'services'
const PLACEHOLDER = '/assets/placeholder-service.svg'

/**
 * Resolves a service image URL.
 * - Empty → default placeholder.
 * - HTTPS → returned as-is.
 * - Relative path → resolved as Supabase Storage public URL.
 * - Anything else (http://, //, protocol:) → placeholder for security.
 */
export function resolveServiceImageUrl(url?: string | null): string {
  const value = url?.trim()

  if (!value) {
    return PLACEHOLDER
  }

  if (value.startsWith('https://')) {
    return value
  }

  if (value.startsWith('http://') || value.startsWith('//') || value.includes(':')) {
    return PLACEHOLDER
  }

  // Resolve as Supabase Storage Public URL
  // Format: https://[project-id].supabase.co/storage/v1/object/public/[bucket]/[path]
  return `${env.supabaseUrl}/storage/v1/object/public/${STORAGE_BUCKET}/${value}`
}
