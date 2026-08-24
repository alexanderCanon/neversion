import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  AssignmentsApiService,
  SuggestAssignmentResponse,
  ConfirmAssignmentResponse,
  ConfirmAssignmentRequest,
  ManualAssignmentRequest,
  ManualAssignmentResponse
} from '@alexandercanon/api-client-angular';

@Injectable({ providedIn: 'root' })
export class AssignmentsService {
  private readonly assignmentsApi = inject(AssignmentsApiService);

  suggestAssignment(orderId: string): Observable<SuggestAssignmentResponse> {
    return this.assignmentsApi.suggestAssignment(orderId);
  }

  confirmAssignment(orderId: string, profileId: string): Observable<ConfirmAssignmentResponse> {
    const request: ConfirmAssignmentRequest = { profileId };
    return this.assignmentsApi.confirmAssignment(orderId, request);
  }

  manualAssignment(request: ManualAssignmentRequest): Observable<ManualAssignmentResponse> {
    return this.assignmentsApi.manualAssignment(request);
  }
}
