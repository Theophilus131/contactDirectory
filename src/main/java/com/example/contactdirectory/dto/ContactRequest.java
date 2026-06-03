package com.example.contactdirectory.dto;

import com.example.contactdirectory.entity.ContactGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Incoming DTO for creating or fully updating a contact.
 * createdAt is intentionally excluded — it is server-managed and immutable.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for creating or updating a contact")
public class ContactRequest {

    @NotBlank(message = "First name must not be blank")
    @Schema(description = "Contact's first name", example = "Jane", requiredMode = Schema.RequiredMode.REQUIRED)
    private String firstName;

    @NotBlank(message = "Last name must not be blank")
    @Schema(description = "Contact's last name", example = "Doe", requiredMode = Schema.RequiredMode.REQUIRED)
    private String lastName;

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email must be a valid email address")
    @Schema(description = "Unique email address", example = "jane.doe@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "Phone number must not be blank")
    @Pattern(regexp = "\\d{10,15}", message = "Phone number must contain digits only and be 10–15 characters long")
    @Schema(description = "Phone number — digits only, 10 to 15 characters", example = "08012345678", requiredMode = Schema.RequiredMode.REQUIRED)
    private String phoneNumber;

    @NotNull(message = "Group must not be null")
    @Schema(description = "Contact group classification", example = "FRIEND", allowableValues = {"FAMILY", "FRIEND", "WORK", "OTHER"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private ContactGroup group;
}
