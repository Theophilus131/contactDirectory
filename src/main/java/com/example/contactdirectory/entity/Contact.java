package com.example.contactdirectory.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity representing a contact in the directory.
 * createdAt is set once on first persist and never updated.
 */
@Entity
@Table(
    name = "contacts",
    indexes = {
        @Index(name = "idx_contact_email", columnList = "email", unique = true),
        @Index(name = "idx_contact_group", columnList = "contact_group")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contact {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "contact_group", nullable = false)
    private ContactGroup group;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Automatically sets createdAt before the entity is first persisted.
     * This field is immutable — it will not be updated on subsequent saves.
     */
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }
}
