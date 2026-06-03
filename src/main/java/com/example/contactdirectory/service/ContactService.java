package com.example.contactdirectory.service;

import com.example.contactdirectory.dto.ContactRequest;
import com.example.contactdirectory.dto.ContactResponse;
import com.example.contactdirectory.entity.ContactGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Business logic contract for Contact Directory operations.
 */
public interface ContactService {

    /**
     * Creates a new contact and returns the persisted representation.
     *
     * @throws com.example.contactdirectory.exception.EmailAlreadyExistsException if email is taken
     */
    ContactResponse create(ContactRequest request);

    /**
     * Returns a paginated, optionally filtered list of contacts.
     *
     * @param group  filter by exact group; {@code null} means no group filter
     * @param search case-insensitive contains match on firstName, lastName, or email;
     *               {@code null} or blank means no text filter
     */
    Page<ContactResponse> list(ContactGroup group, String search, Pageable pageable);

    /**
     * Returns a single contact by id.
     *
     * @throws com.example.contactdirectory.exception.ContactNotFoundException if not found
     */
    ContactResponse getById(UUID id);

    /**
     * Fully replaces a contact's mutable fields; createdAt is preserved.
     *
     * @throws com.example.contactdirectory.exception.ContactNotFoundException   if not found
     * @throws com.example.contactdirectory.exception.EmailAlreadyExistsException if email belongs to another contact
     */
    ContactResponse update(UUID id, ContactRequest request);

    /**
     * Deletes a contact by id.
     *
     * @throws com.example.contactdirectory.exception.ContactNotFoundException if not found
     */
    void delete(UUID id);
}
