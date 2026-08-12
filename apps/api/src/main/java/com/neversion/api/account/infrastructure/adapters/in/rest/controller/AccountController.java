package com.neversion.api.account.infrastructure.adapters.in.rest.controller;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.neversion.api.account.application.port.in.CreateAccountUseCase;
import com.neversion.api.account.application.port.in.CreateAccountWithSubscriptionCommand;
import com.neversion.api.account.application.port.in.CreateAccountWithSubscriptionResult;
import com.neversion.api.account.application.port.in.CreateAccountWithSubscriptionUseCase;
import com.neversion.api.account.application.port.in.DeleteAccountUseCase;
import com.neversion.api.account.application.port.in.GetAccountUseCase;
import com.neversion.api.account.application.port.in.ListAccountsUseCase;
import com.neversion.api.account.application.port.in.UpdateAccountUseCase;
import com.neversion.api.account.domain.model.Account;
import com.neversion.api.account.infrastructure.adapters.in.rest.dto.AccountDetailResponse;
import com.neversion.api.account.infrastructure.adapters.in.rest.dto.AccountRequest;
import com.neversion.api.account.infrastructure.adapters.in.rest.dto.AccountResponse;
import com.neversion.api.account.infrastructure.adapters.in.rest.dto.AccountWithSubscriptionRequest;
import com.neversion.api.account.infrastructure.adapters.in.rest.mapper.AccountMapper;
import com.neversion.api.profile.application.port.in.ProfileUseCase;
import com.neversion.api.shared.domain.model.enums.AccountStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping(value = "/api/v1/accounts", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Accounts", description = "Master account management for digital services (EPIC-03)")
public class AccountController {

    private final CreateAccountUseCase createAccountUseCase;
    private final CreateAccountWithSubscriptionUseCase createAccountWithSubscriptionUseCase;
    private final UpdateAccountUseCase updateAccountUseCase;
    private final GetAccountUseCase getAccountUseCase;
    private final ListAccountsUseCase listAccountsUseCase;
    private final DeleteAccountUseCase deleteAccountUseCase;
    private final ProfileUseCase profileUseCase;
    private final AccountMapper accountMapper;

    public AccountController(CreateAccountUseCase createAccountUseCase,
            CreateAccountWithSubscriptionUseCase createAccountWithSubscriptionUseCase,
            UpdateAccountUseCase updateAccountUseCase,
            GetAccountUseCase getAccountUseCase,
            ListAccountsUseCase listAccountsUseCase,
            DeleteAccountUseCase deleteAccountUseCase,
            ProfileUseCase profileUseCase,
            AccountMapper accountMapper) {
        this.createAccountUseCase = createAccountUseCase;
        this.createAccountWithSubscriptionUseCase = createAccountWithSubscriptionUseCase;
        this.updateAccountUseCase = updateAccountUseCase;
        this.getAccountUseCase = getAccountUseCase;
        this.listAccountsUseCase = listAccountsUseCase;
        this.deleteAccountUseCase = deleteAccountUseCase;
        this.profileUseCase = profileUseCase;
        this.accountMapper = accountMapper;
    }

    // ─── US-022: Create ───────────────────────────────────────────────────────

    @PostMapping
    @Operation(summary = "Create account (US-022)",
            description = "Creates a master account for the authenticated vendor. vendorId resolved from JWT.")
    @ApiResponse(responseCode = "201", description = "Account created")
    @ApiResponse(responseCode = "400", description = "Validation or business rule error")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<AccountResponse> create(
            @Valid @RequestBody AccountRequest request,
            JwtAuthenticationToken token) {
        Account account = accountMapper.toDomain(request);
        Account created = createAccountUseCase.create(account, extractExternalId(token));
        return ResponseEntity.status(HttpStatus.CREATED).body(accountMapper.toResponse(created));
    }

    // ─── Unified: Create account + subscription ───────────────────────────────

    @PostMapping("/with-subscription")
    @Operation(summary = "Create account with subscription",
            description = "Creates a master account, auto-generates profiles, and immediately assigns "
                    + "a subscription to an existing client in a single transaction.")
    @ApiResponse(responseCode = "201", description = "Account and subscription created")
    @ApiResponse(responseCode = "400", description = "Validation or business rule error")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<CreateAccountWithSubscriptionResult> createWithSubscription(
            @Valid @RequestBody AccountWithSubscriptionRequest request,
            JwtAuthenticationToken token) {
        CreateAccountWithSubscriptionCommand command = new CreateAccountWithSubscriptionCommand(
                request.email(),
                request.password(),
                request.serviceUuid(),
                request.saleMode(),
                request.renewalDate(),
                request.plan(),
                request.cost(),
                request.source(),
                request.purchasedAt(),
                request.accountNotes(),
                request.maxProfiles(),
                request.clientUuid(),
                request.paymentDueDate(),
                request.priceSold(),
                request.discountApplied(),
                request.subscriptionNotes(),
                request.sendNotification());
        CreateAccountWithSubscriptionResult result =
                createAccountWithSubscriptionUseCase.create(command, extractExternalId(token));
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    // ─── US-023: Update ───────────────────────────────────────────────────────

    @PutMapping("/{id}")
    @Operation(summary = "Update account (US-023)",
            description = "Updates a master account. Only the owner vendor can edit. id/uuid are immutable.")
    @ApiResponse(responseCode = "200", description = "Account updated")
    @ApiResponse(responseCode = "403", description = "Caller does not own this account")
    @ApiResponse(responseCode = "404", description = "Account not found")
    public ResponseEntity<AccountResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody AccountRequest request,
            JwtAuthenticationToken token) {
        Account updates = accountMapper.toDomain(request);
        Account updated = updateAccountUseCase.update(id, updates, extractExternalId(token));
        return ResponseEntity.ok(accountMapper.toResponse(updated));
    }

    // ─── US-024: List by vendor ───────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "List vendor accounts (US-024)",
            description = "Returns all accounts for the authenticated vendor with optional filters.")
    @ApiResponse(responseCode = "200", description = "Account list")
    public ResponseEntity<List<AccountResponse>> listAccounts(
            @RequestParam(required = false) UUID serviceUuid,
            @RequestParam(required = false) AccountStatus status,
            JwtAuthenticationToken token) {
        List<Account> accounts = listAccountsUseCase.listAccounts(
                serviceUuid, status, extractExternalId(token));
        List<AccountResponse> response = accounts.stream()
                .map(account -> accountMapper.toResponse(
                        account,
                        profileUseCase.findByAccountId(account.getId())))
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/vendor/{vendorUuid}")
    @Operation(summary = "List vendor accounts by vendor UUID (US-024, Legacy)",
            description = "Returns all accounts for the given vendor with optional filters.")
    @ApiResponse(responseCode = "200", description = "Account list")
    @ApiResponse(responseCode = "404", description = "Vendor not found")
    public ResponseEntity<List<AccountResponse>> listByVendor(
            @PathVariable UUID vendorUuid,
            @RequestParam(required = false) UUID serviceUuid,
            @RequestParam(required = false) AccountStatus status,
            JwtAuthenticationToken token) {
        List<Account> accounts = listAccountsUseCase.listByVendor(
                vendorUuid, serviceUuid, status, extractExternalId(token));
        List<AccountResponse> response = accounts.stream()
                .map(account -> accountMapper.toResponse(
                        account,
                        profileUseCase.findByAccountId(account.getId())))
                .toList();
        return ResponseEntity.ok(response);
    }


    // ─── US-025: Generate profiles ────────────────────────────────────────────

    @PostMapping("/{id}/profiles/generate")
    @Operation(summary = "Generate profiles (US-025)",
            description = "Generates N new profiles for an account. Validates against service.maxProfiles.")
    @ApiResponse(responseCode = "201", description = "Profiles generated")
    @ApiResponse(responseCode = "400", description = "Exceeds maxProfiles limit")
    @ApiResponse(responseCode = "403", description = "Caller does not own this account")
    public ResponseEntity<Void> generateProfiles(
            @PathVariable UUID id,
            @RequestParam @Min(1) int count,
            JwtAuthenticationToken token) {
        profileUseCase.generate(id, count, extractExternalId(token));
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // ─── US-028: Detail ───────────────────────────────────────────────────────

    @GetMapping("/{id}/detail")
    @Operation(summary = "Account detail with profiles (US-028)",
            description = "Returns full account data + all profiles + summary counters.")
    @ApiResponse(responseCode = "200", description = "Account detail")
    @ApiResponse(responseCode = "403", description = "Caller does not own this account")
    @ApiResponse(responseCode = "404", description = "Account not found")
    public ResponseEntity<AccountDetailResponse> getDetail(
            @PathVariable UUID id,
            JwtAuthenticationToken token) {
        return ResponseEntity.ok(getAccountUseCase.getDetail(id, extractExternalId(token)));
    }

    // ─── Legacy / generic ─────────────────────────────────────────────────────

    @GetMapping("/{id}")
    @Operation(summary = "Get account by UUID")
    @ApiResponse(responseCode = "200", description = "Account found")
    @ApiResponse(responseCode = "404", description = "Account not found")
    public ResponseEntity<AccountResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(accountMapper.toResponse(getAccountUseCase.getById(id)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete account", description = "Hard-deletes an account. Cascades to profiles.")
    @ApiResponse(responseCode = "204", description = "Account deleted")
    @ApiResponse(responseCode = "404", description = "Account not found")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deleteAccountUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /** Extracts the Supabase externalId (sub claim) from the JWT. */
    private String extractExternalId(Principal principal) {
        if (principal instanceof JwtAuthenticationToken jwtToken) {
            return jwtToken.getToken().getSubject();
        }
        throw new IllegalStateException("No JWT principal found in security context");
    }
}
