package com.neversion.api.account.infrastructure.adapters.in.rest.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.neversion.api.account.application.port.in.CreateAccountUseCase;
import com.neversion.api.account.application.port.in.DeleteAccountUseCase;
import com.neversion.api.account.application.port.in.GetAccountUseCase;
import com.neversion.api.account.domain.model.Account;
import com.neversion.api.account.infrastructure.adapters.in.rest.dto.AccountRequest;
import com.neversion.api.account.infrastructure.adapters.in.rest.dto.AccountResponse;
import com.neversion.api.account.infrastructure.adapters.in.rest.mapper.AccountMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/accounts")
@Tag(name = "Accounts", description = "Master account management for digital services")
public class AccountController {

    private final CreateAccountUseCase createAccountUseCase;
    private final GetAccountUseCase getAccountUseCase;
    private final DeleteAccountUseCase deleteAccountUseCase;
    private final AccountMapper accountMapper;

    public AccountController(CreateAccountUseCase createAccountUseCase,
            GetAccountUseCase getAccountUseCase,
            DeleteAccountUseCase deleteAccountUseCase,
            AccountMapper accountMapper) {
        this.createAccountUseCase = createAccountUseCase;
        this.getAccountUseCase = getAccountUseCase;
        this.deleteAccountUseCase = deleteAccountUseCase;
        this.accountMapper = accountMapper;
    }

    @PostMapping
    @Operation(summary = "Create account (CU-A01)",
            description = "Creates a master account. Auto-generates profiles if saleMode=BY_PROFILE.")
    @ApiResponse(responseCode = "201", description = "Account created")
    @ApiResponse(responseCode = "400", description = "Validation or business rule error")
    public ResponseEntity<AccountResponse> create(@Valid @RequestBody AccountRequest request) {
        Account account = accountMapper.toDomain(request);
        Account created = createAccountUseCase.create(account);
        return ResponseEntity.status(HttpStatus.CREATED).body(accountMapper.toResponse(created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get account by UUID")
    @ApiResponse(responseCode = "200", description = "Account found")
    @ApiResponse(responseCode = "404", description = "Account not found")
    public ResponseEntity<AccountResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(accountMapper.toResponse(getAccountUseCase.getById(id)));
    }

    @GetMapping
    @Operation(summary = "List accounts", description = "Get all accounts, optionally filtered by serviceId")
    @ApiResponse(responseCode = "200", description = "Account list")
    public ResponseEntity<List<AccountResponse>> list(
            @RequestParam(required = false) Long serviceId) {
        List<Account> accounts = serviceId != null
                ? getAccountUseCase.getByServiceId(serviceId)
                : getAccountUseCase.getAll();
        return ResponseEntity.ok(accounts.stream().map(accountMapper::toResponse).toList());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete account", description = "Hard-deletes an account. Cascades to profiles.")
    @ApiResponse(responseCode = "204", description = "Account deleted")
    @ApiResponse(responseCode = "404", description = "Account not found")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deleteAccountUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}
