package com.example.contactdirectory.controller;

import com.example.contactdirectory.dto.ContactRequest;
import com.example.contactdirectory.dto.ContactResponse;
import com.example.contactdirectory.entity.ContactGroup;
import com.example.contactdirectory.exception.ContactNotFoundException;
import com.example.contactdirectory.exception.EmailAlreadyExistsException;
import com.example.contactdirectory.exception.GlobalExceptionHandler;
import com.example.contactdirectory.service.ContactService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ContactController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("ContactController slice tests")
class ContactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContactService service;

    private ObjectMapper objectMapper;
    private UUID contactId;
    private ContactResponse sampleResponse;
    private ContactRequest validRequest;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

        contactId = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");

        sampleResponse = ContactResponse.builder()
            .id(contactId)
            .firstName("Jane")
            .lastName("Doe")
            .email("jane.doe@example.com")
            .phoneNumber("08012345678")
            .group(ContactGroup.FRIEND)
            .createdAt(Instant.parse("2026-06-03T10:15:30Z"))
            .build();

        validRequest = ContactRequest.builder()
            .firstName("Jane")
            .lastName("Doe")
            .email("jane.doe@example.com")
            .phoneNumber("08012345678")
            .group(ContactGroup.FRIEND)
            .build();
    }

    // -------------------------------------------------- POST /contacts
    @Test
    @DisplayName("POST /contacts: 201 on valid request")
    void create_validRequest_returns201() throws Exception {
        when(service.create(any())).thenReturn(sampleResponse);

        mockMvc.perform(post("/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", containsString("/contacts/" + contactId)))
            .andExpect(jsonPath("$.id").value(contactId.toString()))
            .andExpect(jsonPath("$.email").value("jane.doe@example.com"))
            .andExpect(jsonPath("$.group").value("FRIEND"));
    }

    @Test
    @DisplayName("POST /contacts: 400 when firstName is blank")
    void create_blankFirstName_returns400() throws Exception {
        validRequest.setFirstName("  ");

        mockMvc.perform(post("/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.fieldErrors[?(@.field=='firstName')]").exists());
    }

    @Test
    @DisplayName("POST /contacts: 400 when email format is invalid")
    void create_invalidEmail_returns400() throws Exception {
        validRequest.setEmail("not-an-email");

        mockMvc.perform(post("/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors[?(@.field=='email')]").exists());
    }

    @Test
    @DisplayName("POST /contacts: 400 when phone number is not digits only")
    void create_nonDigitPhone_returns400() throws Exception {
        validRequest.setPhoneNumber("0801abc567");

        mockMvc.perform(post("/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors[?(@.field=='phoneNumber')]").exists());
    }

    @Test
    @DisplayName("POST /contacts: 409 when email already exists")
    void create_duplicateEmail_returns409() throws Exception {
        when(service.create(any())).thenThrow(new EmailAlreadyExistsException("jane.doe@example.com"));

        mockMvc.perform(post("/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409));
    }

    // -------------------------------------------------- GET /contacts
    @Test
    @DisplayName("GET /contacts: 200 with paged results")
    void list_returns200() throws Exception {
        when(service.list(any(), any(), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(sampleResponse)));

        mockMvc.perform(get("/contacts"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(1)))
            .andExpect(jsonPath("$.content[0].email").value("jane.doe@example.com"));
    }

    @Test
    @DisplayName("GET /contacts: passes group and search params to service")
    void list_withFilters_passesParamsToService() throws Exception {
        when(service.list(eq(ContactGroup.FRIEND), eq("jane"), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(sampleResponse)));

        mockMvc.perform(get("/contacts")
                .param("group", "FRIEND")
                .param("search", "jane"))
            .andExpect(status().isOk());

        verify(service).list(eq(ContactGroup.FRIEND), eq("jane"), any(Pageable.class));
    }

    // -------------------------------------------------- GET /contacts/{id}
    @Test
    @DisplayName("GET /contacts/{id}: 200 when found")
    void getById_found_returns200() throws Exception {
        when(service.getById(contactId)).thenReturn(sampleResponse);

        mockMvc.perform(get("/contacts/{id}", contactId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(contactId.toString()));
    }

    @Test
    @DisplayName("GET /contacts/{id}: 404 when not found")
    void getById_notFound_returns404() throws Exception {
        when(service.getById(contactId)).thenThrow(new ContactNotFoundException(contactId));

        mockMvc.perform(get("/contacts/{id}", contactId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.path").value("/contacts/" + contactId));
    }

    // -------------------------------------------------- PUT /contacts/{id}
    @Test
    @DisplayName("PUT /contacts/{id}: 200 on valid update")
    void update_valid_returns200() throws Exception {
        when(service.update(eq(contactId), any())).thenReturn(sampleResponse);

        mockMvc.perform(put("/contacts/{id}", contactId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(contactId.toString()));
    }

    @Test
    @DisplayName("PUT /contacts/{id}: 404 when not found")
    void update_notFound_returns404() throws Exception {
        when(service.update(eq(contactId), any())).thenThrow(new ContactNotFoundException(contactId));

        mockMvc.perform(put("/contacts/{id}", contactId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
            .andExpect(status().isNotFound());
    }

    // -------------------------------------------------- DELETE /contacts/{id}
    @Test
    @DisplayName("DELETE /contacts/{id}: 204 on success")
    void delete_success_returns204() throws Exception {
        doNothing().when(service).delete(contactId);

        mockMvc.perform(delete("/contacts/{id}", contactId))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /contacts/{id}: 404 when not found")
    void delete_notFound_returns404() throws Exception {
        doThrow(new ContactNotFoundException(contactId)).when(service).delete(contactId);

        mockMvc.perform(delete("/contacts/{id}", contactId))
            .andExpect(status().isNotFound());
    }
}
