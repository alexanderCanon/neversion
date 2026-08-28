import { mkdirSync, writeFileSync } from 'node:fs'
import { dirname, resolve } from 'node:path'

const outputPath = resolve('public/assets/runtime-config.js')

const config = {
  apiUrl: process.env.API_URL ?? process.env.STORE_API_URL ?? '',
  supabaseUrl: process.env.SUPABASE_URL ?? process.env.STORE_SUPABASE_URL ?? '',
  supabaseKey: process.env.SUPABASE_KEY ?? process.env.STORE_SUPABASE_KEY ?? '',
  storeVendorUuid: process.env.STORE_VENDOR_UUID ?? '',
}

const missing = Object.entries({
  API_URL: config.apiUrl,
  SUPABASE_URL: config.supabaseUrl,
  SUPABASE_KEY: config.supabaseKey,
  STORE_VENDOR_UUID: config.storeVendorUuid,
}).filter(([, value]) => !value)

if (missing.length > 0) {
  const names = missing.map(([name]) => name).join(', ')
  throw new Error(`Missing required runtime config variable(s): ${names}`)
}

mkdirSync(dirname(outputPath), { recursive: true })
writeFileSync(
  outputPath,
  `window.__NEVERSION_CONFIG__ = ${JSON.stringify(config, null, 2)};\n`
)

console.log(`Runtime config written to ${outputPath}`)
