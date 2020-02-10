package com.extremecoder.springdynamodb.controller;

import com.amazonaws.services.cloudsearchdomain.model.*;
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

    @GetMapping("/contact/search")
    public List searchContact() {
        SearchRequest searchRequest = new SearchRequest(). //
                // withQuery("date:[1970-01-01T00:00:00Z TO " +
                // tomorrow.toString() + "]"). //
                 withQuery("name: rakib").
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
        List<Document> docsToDelete = new ArrayList<>();
        for (SearchResult searchResult : searchResults) {
            for (Map.Entry<String, BucketInfo> facet : searchResult.getFacets().entrySet()) {
                log.debug("facet: {} {}", facet.getKey(), facet.getValue());
            }
            for (Hit hit : searchResult.getHits().getHit()) {
                DeleteDocument docToDelete = new DeleteDocument(hit.getId());
                docsToDelete.add(docToDelete);
            }
        }
        return docsToDelete;
    }
}
