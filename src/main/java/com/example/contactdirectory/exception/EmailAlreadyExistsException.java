package com.example.contactdirectory.exception;

/**
 * Thrown when an attempt is made to create or update a contact
 * with an email address that already belongs to another contact.
 */
public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String email) {
        super("A contact with email '" + email + "' already exists");
    }
}
