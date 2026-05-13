package com.neversion.api.client.infrastructure.adapters.out;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import com.neversion.api.client.domain.model.Client;
import com.neversion.api.client.domain.port.out.ClientRepositoryPort;
import com.neversion.api.client.infrastructure.adapters.out.mapper.ClientPersistenceMapper;

import jakarta.persistence.criteria.Predicate;

@Repository
public class JpaClientAdapter implements ClientRepositoryPort {

    private final SpringDataClientRepository clientRepo;
    private final ClientPersistenceMapper clientMapper;

    public JpaClientAdapter(SpringDataClientRepository clientRepo,
            ClientPersistenceMapper clientMapper) {
        this.clientRepo = clientRepo;
        this.clientMapper = clientMapper;
    }

    @Override
    public Client save(Client client) {
        ClientEntity entity = clientMapper.toEntity(client);
        ClientEntity saved = clientRepo.saveAndFlush(entity);
        return clientMapper.toDomain(saved);
    }

    @Override
    public Optional<Client> findById(UUID uuid) {
        return clientRepo.findByUuid(uuid).map(clientMapper::toDomain);
    }

    @Override
    public Optional<Client> findByInternalId(Long id) {
        return clientRepo.findById(id).map(clientMapper::toDomain);
    }

    /** US-029 — Lista clientes del vendor con filtros opcionales (null = sin filtro). */
    @Override
    public List<Client> findByVendorId(Long vendorId, String name, String phone, String email) {
        return clientRepo.findAll(
                        clientFilter(vendorId, name, phone, email),
                        Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(clientMapper::toDomain)
                .toList();
    }

    private Specification<ClientEntity> clientFilter(
            Long vendorId, String name, String phone, String email) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("vendorId"), vendorId));

            if (hasText(name)) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        containsIgnoringCase(name)));
            }

            if (hasText(phone)) {
                predicates.add(criteriaBuilder.like(root.get("phone"), contains(phone)));
            }

            if (hasText(email)) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("email")),
                        containsIgnoringCase(email)));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String contains(String value) {
        return "%" + value.trim() + "%";
    }

    private String containsIgnoringCase(String value) {
        return contains(value).toLowerCase(Locale.ROOT);
    }

    /** US-031 — Validación de unicidad de email antes de persistir. */
    @Override
    public Optional<Client> findByEmail(String email) {
        return clientRepo.findByEmail(email).map(clientMapper::toDomain);
    }

    @Override
    public Optional<Client> findByVendorIdAndPhone(Long vendorId, String phone) {
        if (vendorId == null || !hasText(phone)) {
            return Optional.empty();
        }

        String normalizedPhone = normalizePhone(phone);
        return clientRepo.findByVendorId(vendorId).stream()
                .map(clientMapper::toDomain)
                .filter(client -> normalizedPhone.equals(normalizePhone(client.getPhone())))
                .findFirst();
    }

    @Override
    public Optional<Client> findByUserId(Long userId) {
        return clientRepo.findByUserId(userId).map(clientMapper::toDomain);
    }

    @Override
    public List<Client> findByName(String name) {
        return clientRepo.findByName(name).stream()
                .map(clientMapper::toDomain)
                .toList();
    }

    @Override
    public List<Client> findByPhone(String phone) {
        return clientRepo.findByPhone(phone).stream()
                .map(clientMapper::toDomain)
                .toList();
    }

    @Override
    public List<Client> findAll() {
        return clientRepo.findAll().stream()
                .map(clientMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(UUID uuid) {
        clientRepo.findByUuid(uuid).ifPresent(e -> clientRepo.deleteById(e.getId()));
    }

    private String normalizePhone(String phone) {
        if (phone == null) {
            return "";
        }
        return phone.replaceAll("\\D", "");
    }
}
