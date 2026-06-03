package com.example.contactdirectory.mapper;

import com.example.contactdirectory.dto.ContactRequest;
import com.example.contactdirectory.dto.ContactResponse;
import com.example.contactdirectory.entity.Contact;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-03T14:05:44+0100",
    comments = "version: 1.6.0, compiler: javac, environment: Java 17.0.19 (Eclipse Adoptium)"
)
@Component
public class ContactMapperImpl implements ContactMapper {

    @Override
    public Contact toEntity(ContactRequest request) {
        if ( request == null ) {
            return null;
        }

        Contact.ContactBuilder contact = Contact.builder();

        contact.email( normalizeEmail( request.getEmail() ) );
        contact.firstName( trim( request.getFirstName() ) );
        contact.lastName( trim( request.getLastName() ) );
        contact.phoneNumber( trim( request.getPhoneNumber() ) );
        contact.group( request.getGroup() );

        return contact.build();
    }

    @Override
    public ContactResponse toResponse(Contact contact) {
        if ( contact == null ) {
            return null;
        }

        ContactResponse.ContactResponseBuilder contactResponse = ContactResponse.builder();

        contactResponse.id( contact.getId() );
        contactResponse.firstName( contact.getFirstName() );
        contactResponse.lastName( contact.getLastName() );
        contactResponse.email( contact.getEmail() );
        contactResponse.phoneNumber( contact.getPhoneNumber() );
        contactResponse.group( contact.getGroup() );
        contactResponse.createdAt( contact.getCreatedAt() );

        return contactResponse.build();
    }

    @Override
    public void updateEntityFromRequest(ContactRequest request, Contact contact) {
        if ( request == null ) {
            return;
        }

        contact.setEmail( normalizeEmail( request.getEmail() ) );
        contact.setFirstName( trim( request.getFirstName() ) );
        contact.setLastName( trim( request.getLastName() ) );
        contact.setPhoneNumber( trim( request.getPhoneNumber() ) );
        contact.setGroup( request.getGroup() );
    }
}
