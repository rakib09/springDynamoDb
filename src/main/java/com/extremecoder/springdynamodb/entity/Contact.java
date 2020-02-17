package com.extremecoder.springdynamodb.entity;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAutoGeneratedKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@DynamoDBTable(tableName = "contact")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Contact implements Serializable {

	private static final long serialVersionUID = 1L;

	@JsonProperty("contactId")
	private String contactId;
	@JsonProperty("name")
	private String name;
	@JsonProperty("mobile")
	private String mobile;
	@JsonProperty("email")
	private String email;

	@DynamoDBHashKey(attributeName = "contactId")
	@DynamoDBAutoGeneratedKey
	public String getContactId() {
		return contactId;
	}

	public void setContactId(String contactId) {
		this.contactId = contactId;
	}

	@DynamoDBAttribute
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@DynamoDBAttribute
	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	@DynamoDBAttribute
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}