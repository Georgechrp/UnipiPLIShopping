package com.unipi.george.unipiplishopping.models;

import com.google.firebase.Timestamp;

public class Order {
    private String customerName;
    private String customerId;
    private String productCode;
    private String documentId; // Νέο πεδίο για το document ID
    private Timestamp timestamp;

    public Order(String customerName,String customerId, String productCode, String documentId, Timestamp timestamp) {
        this.customerName = customerName;
        this.customerId = customerId;
        this.productCode = productCode;
        this.documentId = documentId;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}