package com.example.contactdirectory.exception;

import java.util.UUID;

/**
 * Thrown when a requested contact cannot be found by id.
 */
public class ContactNotFoundException extends RuntimeException {

    public ContactNotFoundException(UUID id) {
        super("Contact not found with id: " + id);
    }
}
