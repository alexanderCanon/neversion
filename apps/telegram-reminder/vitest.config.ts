import { defineWorkersConfig } from "@cloudflare/vitest-pool-workers/config";

export default defineWorkersConfig({
	test: {
		poolOptions: {
			workers: {
				wrangler: { configPath: "./wrangler.jsonc" },
				miniflare: {
					bindings: {
						SUPABASE_URL: "https://test.supabase.co",
						SUPABASE_SERVICE_ROLE_KEY: "test-service-role-key",
						TELEGRAM_BOT_TOKEN: "test-bot-token",
						TELEGRAM_CHAT_ID: "123456789",
					},
				},
			},
		},
	},
});
