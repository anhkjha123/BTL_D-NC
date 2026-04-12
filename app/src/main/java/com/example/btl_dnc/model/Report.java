package com.example.btl_dnc.model;

import com.google.firebase.Timestamp;

public class Report {
    public String id;
    public String userId;
    public String name;
    public String email;
    public String content;
    public String type;
    public long createdAt;
    public String status;
    public String title;
    public String imageBase64;
    public Timestamp updateAt;

    public void setId(String id) {
        this.id = id;
    }
    public String userAvatar;
    public Report() {}
}