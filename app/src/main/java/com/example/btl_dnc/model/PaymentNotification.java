package com.example.btl_dnc.model;

import com.google.firebase.Timestamp;

public class PaymentNotification {
    public String id;
    public String authorID;
    public String title;
    public String content;

    public Timestamp startDate; // Ngày bắt đầu đóng
    public Timestamp endDate;
    public String qrCodeBase64;
    public String thumbnailBase64;

    public Timestamp createdAt;

    // Firebase requires a no-argument constructor
    public PaymentNotification() {}

    // Add getters and setters to handle potential type mismatches in Firestore (String vs Number)
    // Firestore uses setters during deserialization if they match the field name.


}