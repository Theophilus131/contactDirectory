package com.example.contactdirectory.repository;

import com.example.contactdirectory.entity.Contact;
import com.example.contactdirectory.entity.ContactGroup;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Thread-safe in-memory implementation of contact storage.
 * Active when spring.profiles.active=inmemory.
 *
 * <p>This implementation intentionally mirrors the JPA repository interface
 * so the service layer works with both storage backends transparently.
 */
@Repository
@Profile("inmemory")
public class InMemoryContactRepository {

    private final Map<UUID, Contact> store = new ConcurrentHashMap<>();

    public Contact save(Contact contact) {
        if (contact.getId() == null) {
            contact.setId(UUID.randomUUID());
        }
        if (contact.getCreatedAt() == null) {
            contact.setCreatedAt(Instant.now());
        }
        store.put(contact.getId(), contact);
        return contact;
    }

    public Optional<Contact> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    public boolean existsById(UUID id) {
        return store.containsKey(id);
    }

    public void deleteById(UUID id) {
        store.remove(id);
    }

    public boolean existsByEmailIgnoreCase(String email) {
        return store.values().stream()
            .anyMatch(c -> c.getEmail().equalsIgnoreCase(email));
    }

    public boolean existsByEmailIgnoreCaseAndIdNot(String email, UUID excludeId) {
        return store.values().stream()
            .anyMatch(c -> c.getEmail().equalsIgnoreCase(email) && !c.getId().equals(excludeId));
    }

    public Optional<Contact> findByEmailIgnoreCase(String email) {
        return store.values().stream()
            .filter(c -> c.getEmail().equalsIgnoreCase(email))
            .findFirst();
    }

    /**
     * Filters contacts by optional group and/or search term with pagination support.
     */
    public Page<Contact> findByGroupAndSearch(ContactGroup group, String search, Pageable pageable) {
        List<Contact> results = store.values().stream()
            .filter(c -> group == null || c.getGroup() == group)
            .filter(c -> {
                if (search == null || search.isBlank()) return true;
                String term = search.toLowerCase();
                return c.getFirstName().toLowerCase().contains(term)
                    || c.getLastName().toLowerCase().contains(term)
                    || c.getEmail().toLowerCase().contains(term);
            })
            .sorted(Comparator.comparing(Contact::getLastName, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(Contact::getFirstName, String.CASE_INSENSITIVE_ORDER))
            .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), results.size());
        List<Contact> page = (start <= end) ? results.subList(start, end) : Collections.emptyList();

        return new PageImpl<>(page, pageable, results.size());
    }

    public long count() {
        return store.size();
    }

    public List<Contact> findAll() {
        return new ArrayList<>(store.values());
    }
}
