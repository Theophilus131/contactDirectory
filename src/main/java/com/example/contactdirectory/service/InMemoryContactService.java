package com.example.contactdirectory.service;

import com.example.contactdirectory.dto.ContactRequest;
import com.example.contactdirectory.dto.ContactResponse;
import com.example.contactdirectory.entity.Contact;
import com.example.contactdirectory.entity.ContactGroup;
import com.example.contactdirectory.exception.ContactNotFoundException;
import com.example.contactdirectory.exception.EmailAlreadyExistsException;
import com.example.contactdirectory.mapper.ContactMapper;
import com.example.contactdirectory.repository.InMemoryContactRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * In-memory backed implementation of ContactService.
 * Active when spring.profiles.active=inmemory.
 */
@Service
@Profile("inmemory")
@RequiredArgsConstructor
@Slf4j
public class InMemoryContactService implements ContactService {

    private final InMemoryContactRepository repository;
    private final ContactMapper mapper;

    @Override
    public ContactResponse create(ContactRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());
        if (repository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new EmailAlreadyExistsException(normalizedEmail);
        }
        Contact contact = mapper.toEntity(request);
        Contact saved = repository.save(contact);
        log.debug("Created contact id={}", saved.getId());
        return mapper.toResponse(saved);
    }

    @Override
    public Page<ContactResponse> list(ContactGroup group, String search, Pageable pageable) {
        String searchTerm = (search != null && !search.isBlank()) ? search.trim() : null;
        return repository.findByGroupAndSearch(group, searchTerm, pageable)
            .map(mapper::toResponse);
    }

    @Override
    public ContactResponse getById(UUID id) {
        return repository.findById(id)
            .map(mapper::toResponse)
            .orElseThrow(() -> new ContactNotFoundException(id));
    }

    @Override
    public ContactResponse update(UUID id, ContactRequest request) {
        Contact existing = repository.findById(id)
            .orElseThrow(() -> new ContactNotFoundException(id));

        String normalizedEmail = normalizeEmail(request.getEmail());
        if (repository.existsByEmailIgnoreCaseAndIdNot(normalizedEmail, id)) {
            throw new EmailAlreadyExistsException(normalizedEmail);
        }

        mapper.updateEntityFromRequest(request, existing);
        Contact saved = repository.save(existing);
        return mapper.toResponse(saved);
    }

    @Override
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new ContactNotFoundException(id);
        }
        repository.deleteById(id);
    }

    private String normalizeEmail(String email) {
        return (email != null) ? email.trim().toLowerCase() : null;
    }
}
