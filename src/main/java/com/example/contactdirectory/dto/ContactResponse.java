package com.example.contactdirectory.dto;

import com.example.contactdirectory.entity.ContactGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Outgoing DTO representing a contact resource.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response payload representing a contact")
public class ContactResponse {

    @Schema(description = "Unique identifier (UUID)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID id;

    @Schema(description = "Contact's first name", example = "Jane")
    private String firstName;

    @Schema(description = "Contact's last name", example = "Doe")
    private String lastName;

    @Schema(description = "Unique email address", example = "jane.doe@example.com")
    private String email;

    @Schema(description = "Phone number", example = "08012345678")
    private String phoneNumber;

    @Schema(description = "Contact group", example = "FRIEND")
    private ContactGroup group;

    @Schema(description = "ISO-8601 timestamp of when the contact was created (immutable)", example = "2026-06-03T10:15:30Z")
    private Instant createdAt;
}
