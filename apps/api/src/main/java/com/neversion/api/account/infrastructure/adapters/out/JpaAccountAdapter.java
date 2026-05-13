package com.neversion.api.account.infrastructure.adapters.out;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import com.neversion.api.account.domain.model.Account;
import com.neversion.api.account.domain.port.out.AccountRepositoryPort;
import com.neversion.api.account.infrastructure.adapters.out.mapper.AccountPersistenceMapper;
import com.neversion.api.shared.domain.model.enums.AccountStatus;

import jakarta.persistence.criteria.Predicate;

@Repository
public class JpaAccountAdapter implements AccountRepositoryPort {

    private final SpringDataAccountRepository accountRepo;
    private final AccountPersistenceMapper accountMapper;

    public JpaAccountAdapter(SpringDataAccountRepository accountRepo,
            AccountPersistenceMapper accountMapper) {
        this.accountRepo = accountRepo;
        this.accountMapper = accountMapper;
    }

    @Override
    public Account save(Account account) {
        AccountEntity entity = accountMapper.toEntity(account);
        AccountEntity saved = accountRepo.saveAndFlush(entity);
        return accountMapper.toDomain(saved);
    }

    @Override
    public Optional<Account> findById(UUID uuid) {
        return accountRepo.findByUuid(uuid).map(accountMapper::toDomain);
    }

    @Override
    public Optional<Account> findByInternalId(Long id) {
        return accountRepo.findById(id).map(accountMapper::toDomain);
    }

    @Override
    public List<Account> findByServiceId(Long serviceId) {
        return accountRepo.findByServiceId(serviceId).stream()
                .map(accountMapper::toDomain)
                .toList();
    }

    @Override
    public List<Account> findByServiceIdAndVendorId(Long serviceId, Long vendorId) {
        return accountRepo.findByServiceIdAndVendorId(serviceId, vendorId).stream()
                .map(accountMapper::toDomain)
                .toList();
    }

    @Override
    public List<Account> findByRenewalDate(LocalDate renewalDate) {
        return accountRepo.findByRenewalDate(renewalDate).stream()
                .map(accountMapper::toDomain)
                .toList();
    }

    @Override
    public List<Account> findByVendorId(Long vendorId) {
        return accountRepo.findByVendorId(vendorId).stream()
                .map(accountMapper::toDomain)
                .toList();
    }

    @Override
    public List<Account> findByVendorIdFiltered(Long vendorId, Long serviceId, AccountStatus status) {
        return accountRepo.findAll(
                        accountFilter(vendorId, serviceId, status),
                        Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(accountMapper::toDomain)
                .toList();
    }

    private Specification<AccountEntity> accountFilter(
            Long vendorId, Long serviceId, AccountStatus status) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("vendorId"), vendorId));

            if (serviceId != null) {
                predicates.add(criteriaBuilder.equal(root.get("serviceId"), serviceId));
            }

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    @Override
    public List<Account> findAll() {
        return accountRepo.findAll().stream()
                .map(accountMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(UUID uuid) {
        accountRepo.findByUuid(uuid).ifPresent(e -> accountRepo.deleteById(e.getId()));
    }
}
