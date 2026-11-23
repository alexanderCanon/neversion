import { sendTelegramMessage } from './services/telegram.service';

type DigitalEnv = Pick<Env, 'TELEGRAM_BOT_TOKEN' | 'TELEGRAM_CHAT_ID'> & {
  DIGITAL_API: Fetcher;
  REMINDER_API_KEY: string;
};

type Digest = {
  today: string;
  total: number;
  subscriptions: { client: string; service: string; dueOn: string }[];
};

export function formatDigest({ today, total, subscriptions }: Digest): string {
  const title = 'Neversion Digital · Vencimientos';
  const rows = subscriptions.map(({ client, service, dueOn }) =>
    (dueOn === today ? 'Hoy' : dueOn) + ' · ' + client.slice(0, 65) + ' · ' + service.slice(0, 65));
  if (!total) return title + '\n\nSin suscripciones por vencer en los próximos tres días.';
  const omitted = total - subscriptions.length;
  return [title, '', ...rows, ...(omitted > 0 ? ['', 'Otros vencimientos: ' + omitted] : [])].join('\n');
}

export default {
  async scheduled(_controller: ScheduledController, env: DigitalEnv) {
    const key = env.REMINDER_API_KEY;
    if (!key || key.length < 32) throw new Error('Falta REMINDER_API_KEY.');
    const response = await env.DIGITAL_API.fetch('https://neversion-digital.internal/internal/renewal-digest', {
      headers: { Authorization: 'Bearer ' + key },
    });
    if (!response.ok) {
      await response.body?.cancel();
      throw new Error('El API de Neversion Digital devolvió HTTP ' + response.status);
    }
    const digest = await response.json<Digest>();
    try {
      await sendTelegramMessage(env, formatDigest(digest), undefined, undefined, 'plain');
    } catch {
      throw new Error('Telegram no aceptó el recordatorio de Neversion Digital.');
    }
  },
} satisfies ExportedHandler<DigitalEnv>;
