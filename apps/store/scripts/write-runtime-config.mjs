import { mkdirSync, writeFileSync } from 'node:fs';
import { dirname, resolve } from 'node:path';
import dotenv from 'dotenv';

dotenv.config();

const outputPath = resolve('src/assets/runtime-config.js');

const config = {
  apiUrl: process.env.STORE_API_URL ?? '',
  supabaseUrl: process.env.STORE_SUPABASE_URL ?? '',
  supabaseKey: process.env.STORE_SUPABASE_KEY ?? '',
  storeVendorUuid: process.env.STORE_VENDOR_UUID ?? '',
};

const missing = Object.entries({
  STORE_API_URL: config.apiUrl,
  STORE_SUPABASE_URL: config.supabaseUrl,
  STORE_SUPABASE_KEY: config.supabaseKey,
  STORE_VENDOR_UUID: config.storeVendorUuid,
}).filter(([, value]) => !value);

if (missing.length > 0) {
  const names = missing.map(([name]) => name).join(', ');
  throw new Error(`Missing required runtime config variable(s): ${names}`);
}

mkdirSync(dirname(outputPath), { recursive: true });
writeFileSync(
  outputPath,
  `window.__NEVERSION_CONFIG__ = ${JSON.stringify(config, null, 2)};\n`,
);

console.log(`Runtime config written to ${outputPath}`);
