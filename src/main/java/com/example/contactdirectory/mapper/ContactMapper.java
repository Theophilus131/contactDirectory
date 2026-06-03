package com.example.contactdirectory.mapper;

import com.example.contactdirectory.dto.ContactRequest;
import com.example.contactdirectory.dto.ContactResponse;
import com.example.contactdirectory.entity.Contact;
import org.mapstruct.*;

/**
 * MapStruct mapper between Contact entity and DTOs.
 *
 * Rules:
 * - createdAt is never mapped from a request (immutable, server-managed).
 * - Emails are lowercased to normalise uniqueness checks.
 * - String inputs are trimmed on inbound mapping.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ContactMapper {

    /**
     * Maps a ContactRequest to a new Contact entity.
     * id and createdAt are ignored — handled by JPA / service layer.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "email", source = "email", qualifiedByName = "normalizeEmail")
    @Mapping(target = "firstName", source = "firstName", qualifiedByName = "trim")
    @Mapping(target = "lastName", source = "lastName", qualifiedByName = "trim")
    @Mapping(target = "phoneNumber", source = "phoneNumber", qualifiedByName = "trim")
    Contact toEntity(ContactRequest request);

    /**
     * Maps a Contact entity to a ContactResponse DTO.
     */
    ContactResponse toResponse(Contact contact);

    /**
     * Applies fields from a ContactRequest onto an existing Contact entity (for PUT).
     * createdAt is explicitly ignored to ensure immutability.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "email", source = "email", qualifiedByName = "normalizeEmail")
    @Mapping(target = "firstName", source = "firstName", qualifiedByName = "trim")
    @Mapping(target = "lastName", source = "lastName", qualifiedByName = "trim")
    @Mapping(target = "phoneNumber", source = "phoneNumber", qualifiedByName = "trim")
    void updateEntityFromRequest(ContactRequest request, @MappingTarget Contact contact);

    @Named("normalizeEmail")
    default String normalizeEmail(String email) {
        return email != null ? email.trim().toLowerCase() : null;
    }

    @Named("trim")
    default String trim(String value) {
        return value != null ? value.trim() : null;
    }
}
