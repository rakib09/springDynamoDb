package com.extremecoder.springdynamodb.serviceImpl;

import com.extremecoder.springdynamodb.entity.Contact;
import com.extremecoder.springdynamodb.repository.ContactRepository;
import com.extremecoder.springdynamodb.service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ContactServiceImpl implements ContactService {

    @Autowired
    private ContactRepository contactRepository;

    @Override
    public Contact saveContact(Contact contact) {
        return contactRepository.save(contact);
    }

    @Override
    public List<Contact> getAllContacts() {
        Iterable<Contact> iterator = contactRepository.findAll();
        List<Contact> list = new ArrayList<>();
        iterator.forEach(list::add);
        return list;
    }

    @Override
    public Contact findByContactId(String contactId) {
        return contactRepository.findContactByContactId(contactId);
    }

    @Override
    public String deleteByContactId(String contactId) {
        Contact contact = contactRepository.findContactByContactId(contactId);
        contactRepository.delete(contact);
        return contactId;
    }

    @Override
    public void updateContact(Contact contact) {
        contactRepository.save(contact);
    }
}
