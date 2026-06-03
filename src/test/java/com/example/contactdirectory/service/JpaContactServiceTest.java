package com.example.contactdirectory.service;

import com.example.contactdirectory.dto.ContactRequest;
import com.example.contactdirectory.dto.ContactResponse;
import com.example.contactdirectory.entity.Contact;
import com.example.contactdirectory.entity.ContactGroup;
import com.example.contactdirectory.exception.ContactNotFoundException;
import com.example.contactdirectory.exception.EmailAlreadyExistsException;
import com.example.contactdirectory.mapper.ContactMapper;
import com.example.contactdirectory.repository.ContactRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JpaContactService unit tests")
class JpaContactServiceTest {

    @Mock
    private ContactRepository repository;

    @Mock
    private ContactMapper mapper;

    @InjectMocks
    private JpaContactService service;

    private UUID contactId;
    private Contact sampleContact;
    private ContactRequest sampleRequest;
    private ContactResponse sampleResponse;

    @BeforeEach
    void setUp() {
        contactId = UUID.randomUUID();

        sampleRequest = ContactRequest.builder()
            .firstName("Jane")
            .lastName("Doe")
            .email("jane.doe@example.com")
            .phoneNumber("08012345678")
            .group(ContactGroup.FRIEND)
            .build();

        sampleContact = Contact.builder()
            .id(contactId)
            .firstName("Jane")
            .lastName("Doe")
            .email("jane.doe@example.com")
            .phoneNumber("08012345678")
            .group(ContactGroup.FRIEND)
            .createdAt(Instant.now())
            .build();

        sampleResponse = ContactResponse.builder()
            .id(contactId)
            .firstName("Jane")
            .lastName("Doe")
            .email("jane.doe@example.com")
            .phoneNumber("08012345678")
            .group(ContactGroup.FRIEND)
            .createdAt(sampleContact.getCreatedAt())
            .build();
    }

    // ---------------------------------------- create()
    @Test
    @DisplayName("create: success — returns ContactResponse with 201-compatible result")
    void create_success() {
        when(repository.existsByEmailIgnoreCase("jane.doe@example.com")).thenReturn(false);
        when(mapper.toEntity(sampleRequest)).thenReturn(sampleContact);
        when(repository.save(sampleContact)).thenReturn(sampleContact);
        when(mapper.toResponse(sampleContact)).thenReturn(sampleResponse);

        ContactResponse result = service.create(sampleRequest);

        assertThat(result).isEqualTo(sampleResponse);
        verify(repository).save(sampleContact);
    }

    @Test
    @DisplayName("create: throws EmailAlreadyExistsException when email is taken")
    void create_duplicateEmail_throwsConflict() {
        when(repository.existsByEmailIgnoreCase("jane.doe@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.create(sampleRequest))
            .isInstanceOf(EmailAlreadyExistsException.class)
            .hasMessageContaining("jane.doe@example.com");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("create: email is normalised to lowercase before uniqueness check")
    void create_emailNormalisedToLowercase() {
        sampleRequest.setEmail("JANE.DOE@EXAMPLE.COM");
        when(repository.existsByEmailIgnoreCase("jane.doe@example.com")).thenReturn(false);
        when(mapper.toEntity(sampleRequest)).thenReturn(sampleContact);
        when(repository.save(any())).thenReturn(sampleContact);
        when(mapper.toResponse(any())).thenReturn(sampleResponse);

        service.create(sampleRequest);

        verify(repository).existsByEmailIgnoreCase("jane.doe@example.com");
    }

    // ---------------------------------------- getById()
    @Test
    @DisplayName("getById: returns response when contact exists")
    void getById_found() {
        when(repository.findById(contactId)).thenReturn(Optional.of(sampleContact));
        when(mapper.toResponse(sampleContact)).thenReturn(sampleResponse);

        assertThat(service.getById(contactId)).isEqualTo(sampleResponse);
    }

    @Test
    @DisplayName("getById: throws ContactNotFoundException when not found")
    void getById_notFound() {
        when(repository.findById(contactId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(contactId))
            .isInstanceOf(ContactNotFoundException.class)
            .hasMessageContaining(contactId.toString());
    }

    // ---------------------------------------- update()
    @Test
    @DisplayName("update: success — createdAt is not overwritten")
    void update_success_createdAtPreserved() {
        Instant originalCreatedAt = sampleContact.getCreatedAt();
        when(repository.findById(contactId)).thenReturn(Optional.of(sampleContact));
        when(repository.existsByEmailIgnoreCaseAndIdNot("jane.doe@example.com", contactId)).thenReturn(false);
        when(repository.save(sampleContact)).thenReturn(sampleContact);
        when(mapper.toResponse(sampleContact)).thenReturn(sampleResponse);

        service.update(contactId, sampleRequest);

        assertThat(sampleContact.getCreatedAt()).isEqualTo(originalCreatedAt);
    }

    @Test
    @DisplayName("update: throws ContactNotFoundException when contact doesn't exist")
    void update_notFound() {
        when(repository.findById(contactId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(contactId, sampleRequest))
            .isInstanceOf(ContactNotFoundException.class);
    }

    @Test
    @DisplayName("update: throws EmailAlreadyExistsException when email belongs to another contact")
    void update_duplicateEmail_throwsConflict() {
        when(repository.findById(contactId)).thenReturn(Optional.of(sampleContact));
        when(repository.existsByEmailIgnoreCaseAndIdNot("jane.doe@example.com", contactId)).thenReturn(true);

        assertThatThrownBy(() -> service.update(contactId, sampleRequest))
            .isInstanceOf(EmailAlreadyExistsException.class);
    }

    // ---------------------------------------- delete()
    @Test
    @DisplayName("delete: success — deleteById is called")
    void delete_success() {
        when(repository.existsById(contactId)).thenReturn(true);

        service.delete(contactId);

        verify(repository).deleteById(contactId);
    }

    @Test
    @DisplayName("delete: throws ContactNotFoundException when id does not exist")
    void delete_notFound() {
        when(repository.existsById(contactId)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(contactId))
            .isInstanceOf(ContactNotFoundException.class);

        verify(repository, never()).deleteById(any());
    }

    // ---------------------------------------- list()
    @Test
    @DisplayName("list: blank search term is normalised to null")
    void list_blankSearch_treatedAsNull() {
        Page<Contact> emptyPage = new PageImpl<>(List.of());
        when(repository.findByGroupAndSearch(isNull(), isNull(), any())).thenReturn(emptyPage);

        service.list(null, "   ", PageRequest.of(0, 10));

        verify(repository).findByGroupAndSearch(isNull(), isNull(), any());
    }

    @Test
    @DisplayName("list: group and search filters are both passed through")
    void list_groupAndSearchPassed() {
        Page<Contact> emptyPage = new PageImpl<>(List.of());
        when(repository.findByGroupAndSearch(eq(ContactGroup.WORK), eq("john"), any())).thenReturn(emptyPage);

        service.list(ContactGroup.WORK, "john", PageRequest.of(0, 10));

        verify(repository).findByGroupAndSearch(eq(ContactGroup.WORK), eq("john"), any());
    }
}
