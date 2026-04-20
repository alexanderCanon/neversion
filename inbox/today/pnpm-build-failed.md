✘ [ERROR] NG5002: "as" expression is only allowed on the primary @if block [plugin angular-compiler]

    src/app/features/reservations/pages/reservation-detail/reservation-detail.component.html:25:31:
      25 │     } @else if (reservation(); as res) {
         ╵                                ~~~~~~

  Error occurs in the template of component ReservationDetailComponent.

    src/app/features/reservations/pages/reservation-detail/reservation-detail.component.ts:14:15:
      14 │   templateUrl: './reservation-detail.component.html',
         ╵                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] NG9: Property 'res' does not exist on type 'ReservationDetailComponent'. [plugin angular-compiler]

    src/app/features/reservations/pages/reservation-detail/reservation-detail.component.html:45:50:
      45 │ ...                       @for (item of res.details; track item.id) {
         ╵                                         ~~~

  Error occurs in the template of component ReservationDetailComponent.

    src/app/features/reservations/pages/reservation-detail/reservation-detail.component.ts:14:15:
      14 │   templateUrl: './reservation-detail.component.html',
         ╵                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] NG9: Property 'res' does not exist on type 'ReservationDetailComponent'. [plugin angular-compiler]

    src/app/features/reservations/pages/reservation-detail/reservation-detail.component.html:57:78:
      57 │ ...s="text-end text-danger">-Q{{ res.discount | number:'1.2-2' }}<...
         ╵                                  ~~~

  Error occurs in the template of component ReservationDetailComponent.

    src/app/features/reservations/pages/reservation-detail/reservation-detail.component.ts:14:15:
      14 │   templateUrl: './reservation-detail.component.html',
         ╵                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] NG9: Property 'res' does not exist on type 'ReservationDetailComponent'. [plugin angular-compiler]

    src/app/features/reservations/pages/reservation-detail/reservation-detail.component.html:61:78:
      61 │ ...ass="text-end fw-bold fs-5">Q{{ res.total | number:'1.2-2' }}</td>
         ╵                                    ~~~

  Error occurs in the template of component ReservationDetailComponent.

    src/app/features/reservations/pages/reservation-detail/reservation-detail.component.ts:14:15:
      14 │   templateUrl: './reservation-detail.component.html',
         ╵                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] NG9: Property 'res' does not exist on type 'ReservationDetailComponent'. [plugin angular-compiler]

    src/app/features/reservations/pages/reservation-detail/reservation-detail.component.html:69:21:
      69 │                 @if (res.receiptUrl) {
         ╵                      ~~~

  Error occurs in the template of component ReservationDetailComponent.

    src/app/features/reservations/pages/reservation-detail/reservation-detail.component.ts:14:15:
      14 │   templateUrl: './reservation-detail.component.html',
         ╵                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] NG9: Property 'res' does not exist on type 'ReservationDetailComponent'. [plugin angular-compiler]

    src/app/features/reservations/pages/reservation-detail/reservation-detail.component.html:75:40:
      75 │ ...                  <img [src]="res.receiptUrl" class="img-fluid ...
         ╵                                  ~~~

  Error occurs in the template of component ReservationDetailComponent.

    src/app/features/reservations/pages/reservation-detail/reservation-detail.component.ts:14:15:
      14 │   templateUrl: './reservation-detail.component.html',
         ╵                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] NG9: Property 'res' does not exist on type 'ReservationDetailComponent'. [plugin angular-compiler]

    src/app/features/reservations/pages/reservation-detail/reservation-detail.component.html:77:43:
      77 │ ...                   <a [href]="res.receiptUrl" target="_blank" c...
         ╵                                  ~~~

  Error occurs in the template of component ReservationDetailComponent.

    src/app/features/reservations/pages/reservation-detail/reservation-detail.component.ts:14:15:
      14 │   templateUrl: './reservation-detail.component.html',
         ╵                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] NG9: Property 'res' does not exist on type 'ReservationDetailComponent'. [plugin angular-compiler]

    src/app/features/reservations/pages/reservation-detail/reservation-detail.component.html:95:60:
      95 │ ...                 'bg-warning text-dark': res.status === 'PENDING',
         ╵                                             ~~~

  Error occurs in the template of component ReservationDetailComponent.

    src/app/features/reservations/pages/reservation-detail/reservation-detail.component.ts:14:15:
      14 │   templateUrl: './reservation-detail.component.html',
         ╵                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] NG9: Property 'res' does not exist on type 'ReservationDetailComponent'. [plugin angular-compiler]

    src/app/features/reservations/pages/reservation-detail/reservation-detail.component.html:96:57:
      96 │ ...                   'bg-info text-dark': res.status === 'UPLOADED',
         ╵                                            ~~~

  Error occurs in the template of component ReservationDetailComponent.

    src/app/features/reservations/pages/reservation-detail/reservation-detail.component.ts:14:15:
      14 │   templateUrl: './reservation-detail.component.html',
         ╵                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] NG9: Property 'res' does not exist on type 'ReservationDetailComponent'. [plugin angular-compiler]

    src/app/features/reservations/pages/reservation-detail/reservation-detail.component.html:97:50:
      97 │ ...                         'bg-success': res.status === 'VALIDATED',
         ╵                                           ~~~

  Error occurs in the template of component ReservationDetailComponent.

    src/app/features/reservations/pages/reservation-detail/reservation-detail.component.ts:14:15:
      14 │   templateUrl: './reservation-detail.component.html',
         ╵                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] NG9: Property 'res' does not exist on type 'ReservationDetailComponent'. [plugin angular-compiler]

    src/app/features/reservations/pages/reservation-detail/reservation-detail.component.html:98:52:
      98 │ ...                         'bg-secondary': res.status === 'EXPIRED',
         ╵                                             ~~~

  Error occurs in the template of component ReservationDetailComponent.

    src/app/features/reservations/pages/reservation-detail/reservation-detail.component.ts:14:15:
      14 │   templateUrl: './reservation-detail.component.html',
         ╵                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] NG9: Property 'res' does not exist on type 'ReservationDetailComponent'. [plugin angular-compiler]

    src/app/features/reservations/pages/reservation-detail/reservation-detail.component.html:99:49:
      99 │ ...                           'bg-danger': res.status === 'CANCELLED'
         ╵                                            ~~~

  Error occurs in the template of component ReservationDetailComponent.

    src/app/features/reservations/pages/reservation-detail/reservation-detail.component.ts:14:15:
      14 │   templateUrl: './reservation-detail.component.html',
         ╵                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] NG9: Property 'res' does not exist on type 'ReservationDetailComponent'. [plugin angular-compiler]

    src/app/features/reservations/pages/reservation-detail/reservation-detail.component.html:100:38:
      100 │                                 }">{{ res.status }}</span>
          ╵                                       ~~~

  Error occurs in the template of component ReservationDetailComponent.

    src/app/features/reservations/pages/reservation-detail/reservation-detail.component.ts:14:15:
      14 │   templateUrl: './reservation-detail.component.html',
         ╵                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] NG9: Property 'res' does not exist on type 'ReservationDetailComponent'. [plugin angular-compiler]

    src/app/features/reservations/pages/reservation-detail/reservation-detail.component.html:104:41:
      104 │ ...                     <span>{{ res.createdAt | date:'short' }}<...
          ╵                                  ~~~

  Error occurs in the template of component ReservationDetailComponent.

    src/app/features/reservations/pages/reservation-detail/reservation-detail.component.ts:14:15:
      14 │   templateUrl: './reservation-detail.component.html',
         ╵                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] NG9: Property 'res' does not exist on type 'ReservationDetailComponent'. [plugin angular-compiler]

    src/app/features/reservations/pages/reservation-detail/reservation-detail.component.html:108:41:
      108 │ ...                     <span>{{ res.expirationDate | date:'short...
          ╵                                  ~~~

  Error occurs in the template of component ReservationDetailComponent.

    src/app/features/reservations/pages/reservation-detail/reservation-detail.component.ts:14:15:
      14 │   templateUrl: './reservation-detail.component.html',
         ╵                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] NG9: Property 'res' does not exist on type 'ReservationDetailComponent'. [plugin angular-compiler]

    src/app/features/reservations/pages/reservation-detail/reservation-detail.component.html:118:29:
      118 │                         @if (res.clientId) {
          ╵                              ~~~

  Error occurs in the template of component ReservationDetailComponent.

    src/app/features/reservations/pages/reservation-detail/reservation-detail.component.ts:14:15:
      14 │   templateUrl: './reservation-detail.component.html',
         ╵                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] NG9: Property 'res' does not exist on type 'ReservationDetailComponent'. [plugin angular-compiler]

    src/app/features/reservations/pages/reservation-detail/reservation-detail.component.html:124:66:
      124 │ ...              <div class="fw-bold small">{{ res.clientId }}</div>
          ╵                                                ~~~

  Error occurs in the template of component ReservationDetailComponent.

    src/app/features/reservations/pages/reservation-detail/reservation-detail.component.ts:14:15:
      14 │   templateUrl: './reservation-detail.component.html',
         ╵                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] NG9: Property 'res' does not exist on type 'ReservationDetailComponent'. [plugin angular-compiler]

    src/app/features/reservations/pages/reservation-detail/reservation-detail.component.html:149:21:
      149 │                 @if (res.status === 'UPLOADED') {
          ╵                      ~~~

  Error occurs in the template of component ReservationDetailComponent.

    src/app/features/reservations/pages/reservation-detail/reservation-detail.component.ts:14:15:
      14 │   templateUrl: './reservation-detail.component.html',
         ╵                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


 ELIFECYCLE  Command failed with exit code 1.