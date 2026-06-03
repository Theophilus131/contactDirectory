package com.example.contactdirectory.service;

import com.example.contactdirectory.dto.ContactRequest;
import com.example.contactdirectory.dto.ContactResponse;
import com.example.contactdirectory.entity.Contact;
import com.example.contactdirectory.entity.ContactGroup;
import com.example.contactdirectory.exception.ContactNotFoundException;
import com.example.contactdirectory.exception.EmailAlreadyExistsException;
import com.example.contactdirectory.mapper.ContactMapper;
import com.example.contactdirectory.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * JPA / H2-backed implementation of ContactService.
 * Active on all profiles except 'inmemory'.
 */
@Service
@Profile("!inmemory")
@RequiredArgsConstructor
@Slf4j
@Transactional
public class JpaContactService implements ContactService {

    private final ContactRepository repository;
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
    @Transactional(readOnly = true)
    public Page<ContactResponse> list(ContactGroup group, String search, Pageable pageable) {
        String searchTerm = (search != null && !search.isBlank()) ? search.trim() : null;
        return repository.findByGroupAndSearch(group, searchTerm, pageable)
            .map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
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
        log.debug("Updated contact id={}", saved.getId());
        return mapper.toResponse(saved);
    }

    @Override
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new ContactNotFoundException(id);
        }
        repository.deleteById(id);
        log.debug("Deleted contact id={}", id);
    }

    // ------------------------------------------------------------------ helpers
    private String normalizeEmail(String email) {
        return (email != null) ? email.trim().toLowerCase() : null;
    }
}
