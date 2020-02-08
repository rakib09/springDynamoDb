package com.extremecoder.springdynamodb.controller;

import com.extremecoder.springdynamodb.entity.Contact;
import com.extremecoder.springdynamodb.service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ContactController {

    @Autowired
    private ContactService contactService;

    @GetMapping("/contact")
    public List<Contact> getAllContacts() {
        return contactService.getAllContacts();
    }

    @GetMapping("/contact/{contactId}")
    public Contact findById(@PathVariable("contactId") String contactId) {
        return contactService.findByContactId(contactId);
    }

    @PostMapping("/contact")
    public Contact saveContact(@RequestBody Contact contact) {
        return contactService.saveContact(contact);
    }

    @PutMapping("/contact/{contactId}")
    public Contact updateContact(@PathVariable("contactId") String id, @RequestBody Contact contact) {
        contactService.updateContact(contact);
        return contact;
    }

    @DeleteMapping("/contact/{contactId}")
    public String deleteById(@PathVariable("contactId") String contactId) {
        return contactService.deleteByContactId(contactId);
    }
}
