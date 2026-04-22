Application bundle generation failed. [22.655 seconds]

✘ [ERROR] Could not resolve "@neversion/api-client"

    src/app/features/accounts/components/account-form/account-form.component.ts:4:25:
      4 │ import { SaleMode } from '@neversion/api-client';
        ╵                          ~~~~~~~~~~~~~~~~~~~~~~~

  You can mark the path "@neversion/api-client" as external to exclude it from the bundle, which will remove this error and leave the unresolved path in the bundle.


✘ [ERROR] TS2307: Cannot find module '@neversion/api-client' or its corresponding type declarations. [plugin angular-compiler]

    src/app/features/accounts/components/account-form/account-form.component.ts:4:41:
      4 │ import { AccountRequest, SaleMode } from '@neversion/api-client';
        ╵                                          ~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] TS2307: Cannot find module '@neversion/api-client' or its corresponding type declarations. [plugin angular-compiler]

    src/app/features/accounts/components/profile-list/profile-list.component.ts:3:48:
      3 │ ...t { ProfileRequest, ProfileResponse } from '@neversion/api-client';
        ╵                                               ~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] TS2307: Cannot find module '@neversion/api-client' or its corresponding type declarations. [plugin angular-compiler]

    src/app/features/accounts/pages/accounts-list/accounts-list.component.ts:5:32:
      5 │ import { AccountResponse } from '@neversion/api-client';
        ╵                                 ~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] TS2307: Cannot find module '@neversion/api-client' or its corresponding type declarations. [plugin angular-compiler]

    src/app/features/accounts/services/accounts.service.ts:4:64:
      4 │ ...st, AccountResponse, AccountsFilter } from '@neversion/api-client';
        ╵                                               ~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] TS2307: Cannot find module '@neversion/api-client' or its corresponding type declarations. [plugin angular-compiler]

    src/app/features/accounts/services/profile.service.ts:4:48:
      4 │ ...t { ProfileRequest, ProfileResponse } from '@neversion/api-client';
        ╵                                               ~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] TS2307: Cannot find module '@neversion/api-client' or its corresponding type declarations. [plugin angular-compiler]

    src/app/features/clients/components/client-form/client-form.component.ts:4:46:
      4 │ import { ClientRequest, ClientResponse } from '@neversion/api-client';
        ╵                                               ~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] TS2307: Cannot find module '@neversion/api-client' or its corresponding type declarations. [plugin angular-compiler]

    src/app/features/clients/pages/clients-list/clients-list.component.ts:5:31:
      5 │ import { ClientResponse } from '@neversion/api-client';
        ╵                                ~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] TS2307: Cannot find module '@neversion/api-client' or its corresponding type declarations. [plugin angular-compiler]

    src/app/features/clients/services/clients.service.ts:4:61:
      4 │ ...uest, ClientResponse, ClientsFilter } from '@neversion/api-client';
        ╵                                               ~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] TS2307: Cannot find module '@neversion/api-client' or its corresponding type declarations. [plugin angular-compiler]

    src/app/features/dashboard/components/account-row/account-row.component.ts:3:29:
      3 │ import { AccountGroup } from '@neversion/api-client';
        ╵                              ~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] TS2307: Cannot find module '@neversion/api-client' or its corresponding type declarations. [plugin angular-compiler]

    src/app/features/dashboard/components/dashboard-metrics/dashboard-metrics.component.ts:4:33:
      4 │ import { DashboardMetrics } from '@neversion/api-client';
        ╵                                  ~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] TS2307: Cannot find module '@neversion/api-client' or its corresponding type declarations. [plugin angular-compiler]

    src/app/features/dashboard/components/product-summary-card/product-summary-card.component.ts:3:31:
      3 │ import { ProductSummary } from '@neversion/api-client';
        ╵                                ~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] TS2307: Cannot find module '@neversion/api-client' or its corresponding type declarations. [plugin angular-compiler]

    src/app/features/dashboard/components/profile-row/profile-row.component.ts:3:28:
      3 │ import { ProfileItem } from '@neversion/api-client';
        ╵                             ~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] TS2307: Cannot find module '@neversion/api-client' or its corresponding type declarations. [plugin angular-compiler]

    src/app/features/dashboard/dashboard.component.ts:5:49:
      5 │ ... { ProductSummary, DashboardMetrics } from '@neversion/api-client';
        ╵                                               ~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] TS2307: Cannot find module '@neversion/api-client' or its corresponding type declarations. [plugin angular-compiler]

    src/app/features/dashboard/pages/product-accounts-page.component.ts:5:42:
      5 │ import { AccountGroup, ProfileItem } from '@neversion/api-client';
        ╵                                           ~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] TS2307: Cannot find module '@neversion/api-client' or its corresponding type declarations. [plugin angular-compiler]

    src/app/features/dashboard/services/master-dashboard.service.ts:5:58:
      5 │ ...tSummary, AccountGroup, ProfileItem } from '@neversion/api-client';
        ╵                                               ~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] TS2307: Cannot find module '@neversion/api-client' or its corresponding type declarations. [plugin angular-compiler]

    src/app/features/orders/pages/orders-list/orders-list.component.ts:6:43:
      6 │ import { OrderResponse, OrderStatus } from '@neversion/api-client';
        ╵                                            ~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] TS2307: Cannot find module '@neversion/api-client' or its corresponding type declarations. [plugin angular-compiler]

    src/app/features/orders/services/orders.service.ts:5:30:
      5 │ import { OrderResponse } from '@neversion/api-client';
        ╵                               ~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] TS2307: Cannot find module '@neversion/api-client' or its corresponding type declarations. [plugin angular-compiler]

    src/app/features/reservations/pages/reservation-detail/reservation-detail.component.ts:6:36:
      6 │ import { ReservationResponse } from '@neversion/api-client';
        ╵                                     ~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] TS2307: Cannot find module '@neversion/api-client' or its corresponding type declarations. [plugin angular-compiler]

    src/app/features/reservations/pages/reservations-list/reservations-list.component.ts:6:55:
      6 │ ...ervationResponse, ReservationStatus } from '@neversion/api-client';
        ╵                                               ~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] TS2307: Cannot find module '@neversion/api-client' or its corresponding type declarations. [plugin angular-compiler]

    src/app/features/reservations/services/reservations.service.ts:9:7:
      9 │ } from '@neversion/api-client';
        ╵        ~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] TS2307: Cannot find module '@neversion/api-client' or its corresponding type declarations. [plugin angular-compiler]

    src/app/features/services/components/service-form/service-form.component.ts:4:31:
      4 │ import { ServiceRequest } from '@neversion/api-client';
        ╵                                ~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] TS2307: Cannot find module '@neversion/api-client' or its corresponding type declarations. [plugin angular-compiler]

    src/app/features/services/components/services-table/services-table.component.ts:3:32:
      3 │ import { ServiceResponse } from '@neversion/api-client';
        ╵                                 ~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] TS2307: Cannot find module '@neversion/api-client' or its corresponding type declarations. [plugin angular-compiler]

    src/app/features/services/pages/services-list/services-list.component.ts:5:48:
      5 │ ...t { ServiceResponse, ServiceRequest } from '@neversion/api-client';
        ╵                                               ~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] TS2307: Cannot find module '@neversion/api-client' or its corresponding type declarations. [plugin angular-compiler]

    src/app/features/services/services/services-data.service.ts:4:48:
      4 │ ...t { ServiceRequest, ServiceResponse } from '@neversion/api-client';
        ╵                                               ~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] TS2307: Cannot find module '@neversion/api-client' or its corresponding type declarations. [plugin angular-compiler]

    src/app/features/subscriptions/components/subscription-form/subscription-form.component.ts:4:42:
      4 │ import { CreateSubscriptionRequest } from '@neversion/api-client';
        ╵                                           ~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] TS2307: Cannot find module '@neversion/api-client' or its corresponding type declarations. [plugin angular-compiler]

    src/app/features/subscriptions/components/subscription-form/subscription-form.component.ts:9:32:
      9 │ import { AccountResponse } from '@neversion/api-client';
        ╵                                 ~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] TS2307: Cannot find module '@neversion/api-client' or its corresponding type declarations. [plugin angular-compiler]

    src/app/features/subscriptions/components/subscription-form/subscription-form.component.ts:10:31:
      10 │ import { ClientResponse } from '@neversion/api-client';
         ╵                                ~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] TS2307: Cannot find module '@neversion/api-client' or its corresponding type declarations. [plugin angular-compiler]

    src/app/features/subscriptions/components/subscription-form/subscription-form.component.ts:11:32:
      11 │ import { ProfileResponse } from '@neversion/api-client';
         ╵                                 ~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] NG5: Argument of type 'unknown' is not assignable to parameter of type 'string'. [plugin angular-compiler]

    src/app/features/subscriptions/pages/subscriptions-list/subscriptions-list.component.html:22:57:
      22 │ ...    <option [value]="status">{{ getStatusLabel(status) }}</option>
         ╵                                                   ~~~~~~

  Error occurs in the template of component SubscriptionsListComponent.

    src/app/features/subscriptions/pages/subscriptions-list/subscriptions-list.component.ts:14:15:
      14 │   templateUrl: './subscriptions-list.component.html',
         ╵                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] Could not resolve "@neversion/api-client"

    src/app/features/subscriptions/pages/subscriptions-list/subscriptions-list.component.ts:6:35:
      6 │ import { SubscriptionStatus } from '@neversion/api-client';
        ╵                                    ~~~~~~~~~~~~~~~~~~~~~~~

  You can mark the path "@neversion/api-client" as external to exclude it from the bundle, which will remove this error and leave the unresolved path in the bundle.


✘ [ERROR] TS2307: Cannot find module '@neversion/api-client' or its corresponding type declarations. [plugin angular-compiler]

    src/app/features/subscriptions/pages/subscriptions-list/subscriptions-list.component.ts:6:78:
      6 │ ...criptionStatus, SubscriptionsFilter } from '@neversion/api-client';
        ╵                                               ~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] TS2307: Cannot find module '@neversion/api-client' or its corresponding type declarations. [plugin angular-compiler]

    src/app/features/subscriptions/services/subscriptions.service.ts:4:85:
      4 │ ...iptionResponse, SubscriptionsFilter } from '@neversion/api-client';
        ╵                                               ~~~~~~~~~~~~~~~~~~~~~~~


 ELIFECYCLE  Command failed with exit code 1.
