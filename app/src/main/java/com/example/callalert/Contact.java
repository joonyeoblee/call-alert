package com.example.callalert;

public class Contact {
    private String name;
    private String phoneNumber;
    private String contactId;

    public Contact(String name, String phoneNumber, String contactId) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.contactId = contactId;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getContactId() {
        return contactId;
    }
}