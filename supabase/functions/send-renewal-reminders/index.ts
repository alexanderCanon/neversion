import { createClient } from 'jsr:@supabase/supabase-js@2';

interface EmailPayload {
  from: string;
  to: string[];
  template: {
    id: string;
    variables: Record<string, unknown>;
  };
}

const RESEND_API_KEY = Deno.env.get('RESEND_API_KEY');
const SENDER_EMAIL = Deno.env.get('SENDER_EMAIL') || 'Neversion <notificaciones@neversion.com>';

const TEMPLATES: Record<string, string> = {
  reminder_7d_client: Deno.env.get('RESEND_TEMPLATE_RENEWAL_7D_CLIENT') || Deno.env.get('RESEND_TEMPLATE_RENEWAL_7D') || 'renewal-reminder-7d',
  reminder_3d_client: Deno.env.get('RESEND_TEMPLATE_RENEWAL_3D_CLIENT') || Deno.env.get('RESEND_TEMPLATE_RENEWAL_3D') || 'renewal-reminder-3d',
  reminder_1d_client: Deno.env.get('RESEND_TEMPLATE_RENEWAL_1D_CLIENT') || Deno.env.get('RESEND_TEMPLATE_RENEWAL_1D') || 'renewal-reminder-1d',
  reminder_7d_vendor: Deno.env.get('RESEND_TEMPLATE_RENEWAL_7D_VENDOR') || 'renewal-reminder-7d-vendor',
  reminder_3d_vendor: Deno.env.get('RESEND_TEMPLATE_RENEWAL_3D_VENDOR') || 'renewal-reminder-3d-vendor',
  reminder_1d_vendor: Deno.env.get('RESEND_TEMPLATE_RENEWAL_1D_VENDOR') || 'renewal-reminder-1d-vendor',
  account_renewal_7d: Deno.env.get('RESEND_TEMPLATE_ACCOUNT_7D') || 'account-renewal-7d',
  account_renewal_3d: Deno.env.get('RESEND_TEMPLATE_ACCOUNT_3D') || 'account-renewal-3d',
  account_renewal_1d: Deno.env.get('RESEND_TEMPLATE_ACCOUNT_1D') || 'account-renewal-1d',
  account_renewal_due: Deno.env.get('RESEND_TEMPLATE_ACCOUNT_DUE') || 'account-renewal-due',
};

Deno.serve(async (req: Request) => {
  if (req.method === 'OPTIONS') {
    return new Response('ok', { headers: { 'Access-Control-Allow-Origin': '*' } });
  }

  try {
    const supabaseUrl = Deno.env.get('SUPABASE_URL')!;
    const supabaseKey = Deno.env.get('SUPABASE_SERVICE_ROLE_KEY')!;
    const supabase = createClient(supabaseUrl, supabaseKey);

    let totalSent = 0;

    // Helper to unwrap objects/arrays from Supabase join results
    const firstObj = (val: any) => (Array.isArray(val) ? val[0] : val);

    // 1. Process Client & Vendor Subscription Reminders (7, 3, 1 days out)
    const subReminderWindows = [
      { days: 7, stageBase: 'reminder_7d', type: 'RENEWAL_REMINDER_7D' },
      { days: 3, stageBase: 'reminder_3d', type: 'RENEWAL_REMINDER_3D' },
      { days: 1, stageBase: 'reminder_1d', type: 'RENEWAL_REMINDER_1D' },
    ];

    for (const window of subReminderWindows) {
      const targetDate = new Date();
      targetDate.setDate(targetDate.getDate() + window.days);
      const targetDateStr = targetDate.toISOString().split('T')[0];

      const { data: subs, error } = await supabase
        .from('subscriptions')
        .select(`
          id,
          uuid,
          payment_due_date,
          status,
          clients!inner ( id, name, email ),
          profiles (
            id,
            name,
            accounts (
              id,
              services ( name ),
              vendors (
                id,
                uuid,
                store_name,
                users ( external_id )
              )
            )
          )
        `)
        .eq('status', 'ACTIVE')
        .eq('payment_due_date', targetDateStr);

      if (error) {
        console.error(`Error querying subscriptions for ${window.days}d:`, error);
        continue;
      }

      if (!subs || subs.length === 0) continue;

      for (const sub of subs) {
        const clientObj = firstObj(sub.clients);
        const profileObj = firstObj(sub.profiles);
        const accountObj = firstObj(profileObj?.accounts);
        const serviceObj = firstObj(accountObj?.services);
        const vendorObj = firstObj(accountObj?.vendors);
        const userObj = firstObj(vendorObj?.users);

        const clientEmail = clientObj?.email;
        const clientName = clientObj?.name || 'Cliente';
        const serviceName = serviceObj?.name || 'Servicio';

        // 1a. Notify Client
        if (clientEmail) {
          const clientStage = `${window.stageBase}_client`;
          const { data: existingClient } = await supabase
            .from('notification_log')
            .select('id')
            .eq('entity_type', 'subscription')
            .eq('entity_id', sub.id)
            .eq('stage', clientStage)
            .maybeSingle();

          if (!existingClient) {
            const templateId = TEMPLATES[clientStage] || `renewal-reminder-${window.days}d`;
            const sentOk = await sendResendEmail({
              from: SENDER_EMAIL,
              to: [clientEmail],
              template: {
                id: templateId,
                variables: {
                  subscriptionId: String(sub.uuid),
                  clientName: String(clientName),
                  serviceName: String(serviceName),
                  paymentDueDate: String(sub.payment_due_date),
                  daysRemaining: String(window.days),
                },
              },
            });

            await supabase.from('notification_log').insert({
              type: window.type,
              recipient_email: clientEmail,
              payload: JSON.stringify({
                subscriptionId: sub.uuid,
                clientName,
                serviceName,
                paymentDueDate: sub.payment_due_date,
                daysRemaining: window.days,
              }),
              status: sentOk ? 'sent' : 'failed',
              entity_type: 'subscription',
              entity_id: sub.id,
              stage: clientStage,
              processed_at: new Date().toISOString(),
            });

            if (sentOk) totalSent++;
          }
        }

        // 1b. Notify Vendor for the Subscription Renewal
        const vendorExternalId = userObj?.external_id;
        if (vendorExternalId) {
          let vendorEmail: string | null = null;
          const { data: authUser } = await supabase.auth.admin.getUserById(vendorExternalId);
          if (authUser && authUser.user) {
            vendorEmail = authUser.user.email || null;
          }

          if (vendorEmail) {
            const vendorStage = `${window.stageBase}_vendor`;
            const { data: existingVendor } = await supabase
              .from('notification_log')
              .select('id')
              .eq('entity_type', 'subscription')
              .eq('entity_id', sub.id)
              .eq('stage', vendorStage)
              .maybeSingle();

            if (!existingVendor) {
              const templateId = TEMPLATES[vendorStage] || `renewal-reminder-${window.days}d-vendor`;
              const sentOk = await sendResendEmail({
                from: SENDER_EMAIL,
                to: [vendorEmail],
                template: {
                  id: templateId,
                  variables: {
                    subscriptionId: String(sub.uuid),
                    clientName: String(clientName),
                    serviceName: String(serviceName),
                    paymentDueDate: String(sub.payment_due_date),
                    daysRemaining: String(window.days),
                  },
                },
              });

              await supabase.from('notification_log').insert({
                type: window.type,
                recipient_email: vendorEmail,
                payload: JSON.stringify({
                  subscriptionId: sub.uuid,
                  clientName,
                  serviceName,
                  paymentDueDate: sub.payment_due_date,
                  daysRemaining: window.days,
                }),
                status: sentOk ? 'sent' : 'failed',
                entity_type: 'subscription',
                entity_id: sub.id,
                stage: vendorStage,
                processed_at: new Date().toISOString(),
              });

              if (sentOk) totalSent++;
            }
          }
        }
      }
    }

    // 2. Process Master Account Renewal Reminders (7, 3, 1, 0 days out)
    const accountReminderWindows = [
      { days: 7, stage: 'account_renewal_7d', type: 'ACCOUNT_RENEWAL_REMINDER_7D' },
      { days: 3, stage: 'account_renewal_3d', type: 'ACCOUNT_RENEWAL_REMINDER_3D' },
      { days: 1, stage: 'account_renewal_1d', type: 'ACCOUNT_RENEWAL_REMINDER_1D' },
      { days: 0, stage: 'account_renewal_due', type: 'ACCOUNT_RENEWAL_REMINDER_DUE' },
    ];

    for (const window of accountReminderWindows) {
      const targetDate = new Date();
      targetDate.setDate(targetDate.getDate() + window.days);
      const targetDateStr = targetDate.toISOString().split('T')[0];

      const { data: accounts, error } = await supabase
        .from('accounts')
        .select(`
          id,
          uuid,
          email,
          renewal_date,
          status,
          services ( name ),
          vendors!inner (
            id,
            uuid,
            store_name,
            users!inner (
              external_id
            )
          )
        `)
        .eq('renewal_date', targetDateStr);

      if (error) {
        console.error(`Error querying accounts for ${window.days}d:`, error);
        continue;
      }

      if (!accounts || accounts.length === 0) continue;

      for (const account of accounts) {
        const stage = window.stage;
        const vendorObj = firstObj(account.vendors);
        const userObj = firstObj(vendorObj?.users);
        const serviceObj = firstObj(account.services);

        const vendorExternalId = userObj?.external_id;
        const storeName = vendorObj?.store_name || 'Vendedor';
        const serviceName = serviceObj?.name || 'Servicio';

        let vendorEmail: string | null = null;
        if (vendorExternalId) {
          const { data: authUser } = await supabase.auth.admin.getUserById(vendorExternalId);
          if (authUser && authUser.user) {
            vendorEmail = authUser.user.email || null;
          }
        }

        if (!vendorEmail) {
          console.warn(`Could not resolve vendor email for account ${account.uuid}, skipping.`);
          continue;
        }

        const { data: existing } = await supabase
          .from('notification_log')
          .select('id')
          .eq('entity_type', 'account')
          .eq('entity_id', account.id)
          .eq('stage', stage)
          .maybeSingle();

        if (existing) continue;

        const templateId = TEMPLATES[stage] || (window.days === 0 ? 'account-renewal-due' : `account-renewal-${window.days}d`);

        const sentOk = await sendResendEmail({
          from: SENDER_EMAIL,
          to: [vendorEmail],
          template: {
            id: templateId,
            variables: {
              accountId: String(account.uuid),
              storeName: String(storeName),
              serviceName: String(serviceName),
              accountEmail: String(account.email),
              renewalDate: String(account.renewal_date),
              daysRemaining: String(window.days),
            },
          },
        });

        await supabase.from('notification_log').insert({
          type: window.type,
          recipient_email: vendorEmail,
          payload: JSON.stringify({
            accountId: account.uuid,
            storeName,
            serviceName,
            accountEmail: account.email,
            renewalDate: account.renewal_date,
            daysRemaining: window.days,
          }),
          status: sentOk ? 'sent' : 'failed',
          entity_type: 'account',
          entity_id: account.id,
          stage: stage,
          processed_at: new Date().toISOString(),
        });

        if (sentOk) totalSent++;
      }
    }

    return new Response(
      JSON.stringify({ success: true, totalSent, message: `Dispatched ${totalSent} renewal notifications` }),
      { headers: { 'Content-Type': 'application/json' }, status: 200 }
    );
  } catch (err: any) {
    console.error('Unhandled error in send-renewal-reminders:', err);
    return new Response(
      JSON.stringify({ success: false, error: err.message }),
      { headers: { 'Content-Type': 'application/json' }, status: 500 }
    );
  }
});

async function sendResendEmail(payload: EmailPayload): Promise<boolean> {
  if (!RESEND_API_KEY) {
    console.warn('RESEND_API_KEY not configured. Skipping email dispatch for:', payload.to);
    return false;
  }

  try {
    const res = await fetch('https://api.resend.com/emails', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${RESEND_API_KEY}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(payload),
    });

    if (!res.ok) {
      const errText = await res.text();
      console.error('Resend API error response:', res.status, errText);
      return false;
    }

    return true;
  } catch (e) {
    console.error('Failed to invoke Resend API:', e);
    return false;
  }
}
