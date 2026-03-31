package com.example.btl_dnc.model;



import com.google.firebase.Timestamp;

public class Comment {
    public String id;
    public String userId;

    public String content;
    public Timestamp time;
    public String role;
    public Comment() {}
}