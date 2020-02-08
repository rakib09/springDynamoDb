package com.extremecoder.springdynamodb.service;

import com.extremecoder.springdynamodb.entity.Contact;

import java.util.List;

public interface ContactService {
    Contact saveContact(Contact contact);
    List<Contact> getAllContacts();
    Contact findByContactId(String contactId);
    String deleteByContactId(String contactId);
    void updateContact(Contact contact);
}
