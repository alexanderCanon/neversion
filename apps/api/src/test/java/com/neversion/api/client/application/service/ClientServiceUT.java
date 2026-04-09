package com.neversion.api.client.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.client.domain.model.Client;
import com.neversion.api.client.domain.port.out.ClientRepositoryPort;

/**
 * Unit tests for ClientService.
 * Validates: create, getById, getByName, getByPhone, getAll, update, delete.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ClientService unit tests")
class ClientServiceUT {

    @Mock
    private ClientRepositoryPort clientRepositoryPort;

    private ClientService clientService;

    private static final UUID CLIENT_UUID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        clientService = new ClientService(clientRepositoryPort);
    }

    private Client buildClient() {
        return Client.builder()
                .id(1L)
                .uuid(CLIENT_UUID)
                .name("Juan Pérez")
                .email("juan@gmail.com")
                .phone("55551234")
                .notes("Regular customer")
                .build();
    }

    // ── create ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("should delegate save to repository")
        void create_shouldDelegateSaveToRepository() {
            // Given
            Client client = buildClient();
            when(clientRepositoryPort.save(client)).thenReturn(client);

            // When
            Client result = clientService.create(client);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Juan Pérez");
            verify(clientRepositoryPort).save(client);
        }
    }

    // ── getById ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("should return client when found")
        void getById_shouldReturnClient_whenFound() {
            // Given
            when(clientRepositoryPort.findById(CLIENT_UUID)).thenReturn(Optional.of(buildClient()));

            // When
            Client result = clientService.getById(CLIENT_UUID);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUuid()).isEqualTo(CLIENT_UUID);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void getById_shouldThrowResourceNotFound_whenNotFound() {
            // Given
            when(clientRepositoryPort.findById(CLIENT_UUID)).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> clientService.getById(CLIENT_UUID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(CLIENT_UUID.toString());
        }
    }

    // ── getByName ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("getByName")
    class GetByName {

        @Test
        @DisplayName("should delegate to repository")
        void getByName_shouldDelegateToRepository() {
            // Given
            when(clientRepositoryPort.findByName("Juan")).thenReturn(List.of(buildClient()));

            // When
            List<Client> result = clientService.getByName("Juan");

            // Then
            assertThat(result).hasSize(1);
        }
    }

    // ── getByPhone ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("getByPhone")
    class GetByPhone {

        @Test
        @DisplayName("should delegate to repository")
        void getByPhone_shouldDelegateToRepository() {
            // Given
            when(clientRepositoryPort.findByPhone("55551234")).thenReturn(List.of(buildClient()));

            // When
            List<Client> result = clientService.getByPhone("55551234");

            // Then
            assertThat(result).hasSize(1);
        }
    }

    // ── getAll ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getAll")
    class GetAll {

        @Test
        @DisplayName("should return all clients")
        void getAll_shouldReturnAllClients() {
            // Given
            when(clientRepositoryPort.findAll()).thenReturn(List.of(buildClient()));

            // When
            List<Client> result = clientService.getAll();

            // Then
            assertThat(result).hasSize(1);
        }
    }

    // ── update ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("should update existing client fields")
        void update_shouldUpdateExistingClientFields() {
            // Given
            Client existing = buildClient();
            Client updated = Client.builder()
                    .name("Juan Updated")
                    .phone("99998888")
                    .email("updated@gmail.com")
                    .notes("VIP")
                    .build();

            when(clientRepositoryPort.findById(CLIENT_UUID)).thenReturn(Optional.of(existing));
            when(clientRepositoryPort.save(existing)).thenReturn(existing);

            // When
            Client result = clientService.update(CLIENT_UUID, updated);

            // Then
            assertThat(result).isNotNull();
            verify(clientRepositoryPort).save(existing);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when client not found")
        void update_shouldThrowResourceNotFound_whenNotFound() {
            // Given
            Client updated = buildClient();
            when(clientRepositoryPort.findById(CLIENT_UUID)).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> clientService.update(CLIENT_UUID, updated))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ── delete ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("should delete client when found")
        void delete_shouldDeleteClient_whenFound() {
            // Given
            when(clientRepositoryPort.findById(CLIENT_UUID)).thenReturn(Optional.of(buildClient()));

            // When
            clientService.delete(CLIENT_UUID);

            // Then
            verify(clientRepositoryPort).deleteById(CLIENT_UUID);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void delete_shouldThrowResourceNotFound_whenNotFound() {
            // Given
            when(clientRepositoryPort.findById(CLIENT_UUID)).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> clientService.delete(CLIENT_UUID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
