package com.example.contactdirectory.config;

import com.example.contactdirectory.dto.ContactRequest;
import com.example.contactdirectory.entity.ContactGroup;
import com.example.contactdirectory.service.ContactService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Seeds the database with sample contacts on startup for quick manual testing.
 * Active on 'h2' and 'inmemory' profiles but NOT on 'test'.
 */
@Component
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final ContactService service;

    @Override
    public void run(String... args) {
        try {
            // Only seed if the directory is empty
            seedIfEmpty();
        } catch (Exception e) {
            log.warn("Data seeding skipped or failed: {}", e.getMessage());
        }
    }

    private void seedIfEmpty() {
        // We check by attempting to list — if there's data we skip
        var page = service.list(null, null,
            org.springframework.data.domain.PageRequest.of(0, 1));
        if (!page.isEmpty()) {
            log.info("Sample data already present — skipping seeder.");
            return;
        }

        log.info("Seeding sample contacts…");

        service.create(ContactRequest.builder()
            .firstName("Alice").lastName("Adeyemi")
            .email("alice.adeyemi@example.com").phoneNumber("08012345678")
            .group(ContactGroup.FAMILY).build());

        service.create(ContactRequest.builder()
            .firstName("Bob").lastName("Okafor")
            .email("bob.okafor@example.com").phoneNumber("08023456789")
            .group(ContactGroup.FRIEND).build());

        service.create(ContactRequest.builder()
            .firstName("Chidi").lastName("Eze")
            .email("chidi.eze@example.com").phoneNumber("08034567890")
            .group(ContactGroup.WORK).build());

        service.create(ContactRequest.builder()
            .firstName("Damilola").lastName("Balogun")
            .email("damilola.balogun@example.com").phoneNumber("07045678901")
            .group(ContactGroup.WORK).build());

        service.create(ContactRequest.builder()
            .firstName("Emeka").lastName("Nwosu")
            .email("emeka.nwosu@example.com").phoneNumber("09056789012")
            .group(ContactGroup.OTHER).build());

        log.info("Seeded 5 sample contacts.");
    }
}
