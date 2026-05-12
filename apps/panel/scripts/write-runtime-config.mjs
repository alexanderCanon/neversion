import { mkdirSync, writeFileSync } from 'node:fs';
import { dirname, resolve } from 'node:path';

const outputPath = resolve('src/assets/runtime-config.js');

const config = {
  apiUrl: process.env.PANEL_API_URL ?? '',
  grafanaUrl: process.env.GRAFANA_URL ?? '',
  supabaseUrl: process.env.PANEL_SUPABASE_URL ?? '',
  supabaseKey: process.env.PANEL_SUPABASE_KEY ?? '',
};

const missing = Object.entries({
  PANEL_API_URL: config.apiUrl,
  PANEL_SUPABASE_URL: config.supabaseUrl,
  PANEL_SUPABASE_KEY: config.supabaseKey,
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
