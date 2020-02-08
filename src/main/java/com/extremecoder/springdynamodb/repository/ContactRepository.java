package com.extremecoder.springdynamodb.repository;

import com.extremecoder.springdynamodb.entity.Contact;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;

@EnableScan
public interface ContactRepository extends CrudRepository<Contact, String> {

    Contact findContactByContactId(String contactId);

}