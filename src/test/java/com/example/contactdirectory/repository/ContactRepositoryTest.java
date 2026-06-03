package com.example.contactdirectory.repository;

import com.example.contactdirectory.entity.Contact;
import com.example.contactdirectory.entity.ContactGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("ContactRepository query tests")
class ContactRepositoryTest {

    @Autowired
    private ContactRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();

        repository.save(contact("Alice", "Adeyemi", "alice@example.com", "08011111111", ContactGroup.FAMILY));
        repository.save(contact("Bob", "Brown", "bob@example.com", "08022222222", ContactGroup.FRIEND));
        repository.save(contact("Charlie", "Chen", "charlie.work@company.com", "08033333333", ContactGroup.WORK));
        repository.save(contact("Diana", "Adeyemi", "diana@example.com", "08044444444", ContactGroup.FAMILY));
    }

    // -------------------------------------------------- existsByEmailIgnoreCase
    @Test
    @DisplayName("existsByEmailIgnoreCase: true when email exists (case-insensitive)")
    void existsByEmail_caseInsensitive() {
        assertThat(repository.existsByEmailIgnoreCase("ALICE@EXAMPLE.COM")).isTrue();
        assertThat(repository.existsByEmailIgnoreCase("Alice@Example.Com")).isTrue();
        assertThat(repository.existsByEmailIgnoreCase("nobody@example.com")).isFalse();
    }

    // -------------------------------------------------- findByGroupAndSearch — group filter
    @Test
    @DisplayName("findByGroupAndSearch: filters by group only")
    void search_groupFilter() {
        Page<Contact> result = repository.findByGroupAndSearch(ContactGroup.FAMILY, null, PageRequest.of(0, 10));
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).extracting(Contact::getGroup)
            .containsOnly(ContactGroup.FAMILY);
    }

    // -------------------------------------------------- findByGroupAndSearch — search filter
    @Test
    @DisplayName("findByGroupAndSearch: case-insensitive firstName search")
    void search_byFirstName_caseInsensitive() {
        Page<Contact> result = repository.findByGroupAndSearch(null, "alice", PageRequest.of(0, 10));
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getFirstName()).isEqualTo("Alice");
    }

    @Test
    @DisplayName("findByGroupAndSearch: lastName search returns multiple hits")
    void search_byLastName_multipleHits() {
        Page<Contact> result = repository.findByGroupAndSearch(null, "adeyemi", PageRequest.of(0, 10));
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("findByGroupAndSearch: email contains search")
    void search_byEmailContains() {
        Page<Contact> result = repository.findByGroupAndSearch(null, "work", PageRequest.of(0, 10));
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("charlie.work@company.com");
    }

    // -------------------------------------------------- combined group + search
    @Test
    @DisplayName("findByGroupAndSearch: combined group AND search filter")
    void search_groupAndSearch_combined() {
        // 'adeyemi' matches two contacts but only one is FAMILY and named 'alice'
        Page<Contact> result = repository.findByGroupAndSearch(ContactGroup.FAMILY, "alice", PageRequest.of(0, 10));
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getFirstName()).isEqualTo("Alice");
    }

    @Test
    @DisplayName("findByGroupAndSearch: no match returns empty page")
    void search_noMatch_emptyPage() {
        Page<Contact> result = repository.findByGroupAndSearch(null, "zzz_nonexistent", PageRequest.of(0, 10));
        assertThat(result.getTotalElements()).isZero();
    }

    // -------------------------------------------------- pagination
    @Test
    @DisplayName("findByGroupAndSearch: pagination works correctly")
    void search_pagination() {
        Page<Contact> page1 = repository.findByGroupAndSearch(null, null, PageRequest.of(0, 2));
        Page<Contact> page2 = repository.findByGroupAndSearch(null, null, PageRequest.of(1, 2));

        assertThat(page1.getTotalElements()).isEqualTo(4);
        assertThat(page1.getContent()).hasSize(2);
        assertThat(page2.getContent()).hasSize(2);
    }

    // -------------------------------------------------- helper
    private Contact contact(String first, String last, String email, String phone, ContactGroup group) {
        return Contact.builder()
            .firstName(first).lastName(last)
            .email(email).phoneNumber(phone)
            .group(group).createdAt(Instant.now())
            .build();
    }
}
