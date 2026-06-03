package com.example.contactdirectory.controller;

import com.example.contactdirectory.dto.ContactRequest;
import com.example.contactdirectory.dto.ContactResponse;
import com.example.contactdirectory.dto.ErrorResponse;
import com.example.contactdirectory.entity.ContactGroup;
import com.example.contactdirectory.service.ContactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

/**
 * REST controller exposing all Contact Directory endpoints.
 */
@RestController
@RequestMapping("/contacts")
@RequiredArgsConstructor
@Tag(name = "Contacts", description = "Manage contacts in the directory")
public class ContactController {

    private final ContactService service;

    // ================================================================== POST /contacts
    @PostMapping
    @Operation(
        summary = "Create a new contact",
        description = "Creates a contact and returns 201 with a Location header pointing to the new resource."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201", description = "Contact created successfully",
            headers = @Header(name = "Location", description = "URI of the newly created contact"),
            content = @Content(schema = @Schema(implementation = ContactResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Validation failed",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Email already in use",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ContactResponse> create(
        @Valid @RequestBody ContactRequest request
    ) {
        ContactResponse created = service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.getId())
            .toUri();
        return ResponseEntity.created(location).body(created);
    }

    // ================================================================== GET /contacts
    @GetMapping
    @Operation(
        summary = "List contacts",
        description = """
            Returns a paginated list of contacts.
            Optionally filter by `group` (exact enum match) and/or `search` (case-insensitive contains
            on firstName, lastName, or email). When both are provided they are ANDed together.
            Default sort: lastName ASC, firstName ASC.
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Contacts retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid query parameter value",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Page<ContactResponse>> list(
        @Parameter(description = "Filter by contact group (exact match)", example = "FRIEND")
        @RequestParam(required = false) ContactGroup group,

        @Parameter(description = "Case-insensitive search on firstName, lastName, or email", example = "jane")
        @RequestParam(required = false) String search,

        @ParameterObject
        @PageableDefault(size = 20, sort = {"lastName", "firstName"}, direction = Sort.Direction.ASC)
        Pageable pageable
    ) {
        return ResponseEntity.ok(service.list(group, search, pageable));
    }

    // ================================================================== GET /contacts/{id}
    @GetMapping("/{id}")
    @Operation(summary = "Get a contact by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Contact found",
            content = @Content(schema = @Schema(implementation = ContactResponse.class))),
        @ApiResponse(responseCode = "404", description = "Contact not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ContactResponse> getById(
        @Parameter(description = "UUID of the contact", required = true)
        @PathVariable UUID id
    ) {
        return ResponseEntity.ok(service.getById(id));
    }

    // ================================================================== PUT /contacts/{id}
    @PutMapping("/{id}")
    @Operation(
        summary = "Full update of a contact",
        description = "Replaces all mutable fields. createdAt is immutable and will not change."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Contact updated successfully",
            content = @Content(schema = @Schema(implementation = ContactResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation failed",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Contact not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Email already in use by another contact",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ContactResponse> update(
        @Parameter(description = "UUID of the contact to update", required = true)
        @PathVariable UUID id,
        @Valid @RequestBody ContactRequest request
    ) {
        return ResponseEntity.ok(service.update(id, request));
    }

    // ================================================================== DELETE /contacts/{id}
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a contact by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Contact deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Contact not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> delete(
        @Parameter(description = "UUID of the contact to delete", required = true)
        @PathVariable UUID id
    ) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
