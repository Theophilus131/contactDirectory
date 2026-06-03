package com.example.contactdirectory.repository;

import com.example.contactdirectory.entity.Contact;
import com.example.contactdirectory.entity.ContactGroup;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for Contact entities.
 * Active under the 'h2' profile (default).
 */
@Repository
@Profile("!inmemory")
public interface ContactRepository extends JpaRepository<Contact, UUID> {

    /**
     * Checks if a contact with the given email exists, excluding a specific contact by id.
     * Used for uniqueness validation on both create and update.
     */
    boolean existsByEmailIgnoreCaseAndIdNot(String email, UUID excludeId);

    /**
     * Checks if any contact with the given email exists.
     * Used for uniqueness validation on create.
     */
    boolean existsByEmailIgnoreCase(String email);

    /**
     * Lists all contacts optionally filtered by group and/or a search term.
     *
     * <p>Filter logic (all ANDed):
     * <ul>
     *   <li>:group — exact match on group enum; if null, all groups are included.</li>
     *   <li>:search — case-insensitive contains match on firstName, lastName, or email.</li>
     * </ul>
     */
    @Query("""
        SELECT c FROM Contact c
        WHERE (:group IS NULL OR c.group = :group)
          AND (
            :search IS NULL
            OR LOWER(c.firstName) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(c.lastName)  LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(c.email)     LIKE LOWER(CONCAT('%', :search, '%'))
          )
        ORDER BY c.lastName ASC, c.firstName ASC
        """)
    Page<Contact> findByGroupAndSearch(
        @Param("group") ContactGroup group,
        @Param("search") String search,
        Pageable pageable
    );

    Optional<Contact> findByEmailIgnoreCase(String email);
}
