package com.example.btl_dnc.model;

import com.google.firebase.Timestamp;

public class News {
    public String id;
    public String authorID;
    public String content;
    public Timestamp createAt;
    public Timestamp updateAt;
    public String imageBase64;
    public String title;
    public Timestamp getCreateAt() {
        return createAt;
    }

    public Timestamp getUpdateAt() {
        return updateAt;
    }

    public News() {}
}
