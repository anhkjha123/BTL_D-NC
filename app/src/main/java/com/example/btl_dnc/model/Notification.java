package com.example.btl_dnc.model;

import com.google.firebase.Timestamp;

public class Notification {
    public String id;
    public String userId;
    public String title;
    public String message;
    public String type;
    public String refId;
    public boolean isRead;
    public Timestamp createdAt;

    public Notification() {}
}
