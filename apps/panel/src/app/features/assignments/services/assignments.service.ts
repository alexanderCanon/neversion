import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  AssignmentsApiService,
  SuggestAssignmentResponse,
  ConfirmAssignmentResponse,
  ConfirmAssignmentRequest,
  ManualAssignmentRequest,
  ManualAssignmentResponse
} from '@neversion/api-client';

@Injectable({ providedIn: 'root' })
export class AssignmentsService {
  private readonly assignmentsApi = inject(AssignmentsApiService);

  suggestAssignment(orderId: string): Observable<SuggestAssignmentResponse> {
    return this.assignmentsApi.suggest(orderId);
  }

  confirmAssignment(orderId: string, profileId: string): Observable<ConfirmAssignmentResponse> {
    const request: ConfirmAssignmentRequest = { profileId };
    return this.assignmentsApi.confirm(orderId, request);
  }

  manualAssignment(request: ManualAssignmentRequest): Observable<ManualAssignmentResponse> {
    return this.assignmentsApi.manual(request);
  }
}
