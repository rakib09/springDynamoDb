package com.extremecoder.springdynamodb.controller;

import com.amazonaws.services.cloudsearchdomain.model.*;
import com.extremecoder.springdynamodb.docs.AddDocument;
import com.extremecoder.springdynamodb.docs.DeleteDocument;
import com.extremecoder.springdynamodb.docs.Document;
import com.extremecoder.springdynamodb.entity.Contact;
import com.extremecoder.springdynamodb.service.ContactService;
import com.extremecoder.springdynamodb.serviceImpl.CloudSearchClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.amazonaws.services.cloudsearchdomain.model.QueryParser.Lucene;
import static com.extremecoder.springdynamodb.serviceImpl.CloudSearchClient.*;

@RestController
@RequestMapping("/api")
public class ContactController {

    private static final Logger log = LoggerFactory.getLogger(ContactController.class);

    @Autowired
    private CloudSearchClient cloudSearchClient;


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

    @GetMapping("/contact/search/{name}")
    public List searchContact(@PathVariable("name") String name) {
        SearchRequest searchRequest = new SearchRequest(). //
                // withQuery("date:[1970-01-01T00:00:00Z TO " +
                // tomorrow.toString() + "]"). //
                 withQuery("name: " + name).
                // withSort("date asc"). //
//                        withQuery("*:*"). //
                // withFacet(Facet.toJson("integer_i")). //
                // withFacet(Facet.toJson("list_of_integers_is")). //
                        withCursor(INITIAL_CURSOR). //
                withSize(10000L). //
                withReturn(ALL_FIELDS). //
                withQueryParser(Lucene);
        log.info("search start");
        List<SearchResult> searchResults = cloudSearchClient.search("contact", searchRequest);
        log.info("search finish");
        List contactList = new ArrayList<>();
        for (SearchResult searchResult : searchResults) {
            for (Hit hit : searchResult.getHits().getHit()) {
                contactList.add(hit.getFields());
            }
        }
        return contactList;
    }

    @PostMapping("/upload")
    public String upload(@RequestBody Contact contact) {
        List<Document> docs = new ArrayList<>();
        log.info("upload start");
        Contact contactUploadedToDB = contactService.saveContact(contact);
        AddDocument addDocument = AddDocument
                .withRandomId()
                .withField("contactid",contactUploadedToDB.getContactId())
                .withField("name", contactUploadedToDB.getName())
                .withField("email", contactUploadedToDB.getEmail())
                .withField("mobile", contactUploadedToDB.getMobile())
                ;
        docs.add(addDocument);
        List<UploadDocumentsResult> uploadDocumentsResults = cloudSearchClient.uploadDocuments("contact", docs);
        for (UploadDocumentsResult uploadDocumentsResult : uploadDocumentsResults) {
            log.info("uploadDocumentsResult: {}", uploadDocumentsResult);
        }

        return "Success";
    }
}
