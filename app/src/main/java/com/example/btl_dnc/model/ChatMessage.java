package com.example.btl_dnc.model;

import com.google.firebase.Timestamp;

public class ChatMessage {
    public String message;
    public boolean isUser;
    public Timestamp time;

    public ChatMessage() {}

    public ChatMessage(String message, boolean isUser, Timestamp time) {
        this.message = message;
        this.isUser = isUser;
        this.time = time;
    }
}