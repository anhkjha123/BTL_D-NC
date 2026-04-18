package com.example.btl_dnc.model;

import com.google.firebase.Timestamp;

public class ManagerNotification {
    public String id;
    public String title;
    public String message;
    public String type; // Ví dụ: "INCIDENT_ALERT"
    public String incidentId; // ID của sự cố để khi bấm vào có thể mở chi tiết
    private boolean isRead;

    @com.google.firebase.firestore.PropertyName("isRead")
    public boolean isRead() {
        return isRead;
    }

    @com.google.firebase.firestore.PropertyName("isRead")
    public void setIsRead(Object isRead) {
        if (isRead instanceof Boolean) {
            this.isRead = (Boolean) isRead;
        } else if (isRead instanceof String) {
            this.isRead = Boolean.parseBoolean((String) isRead);
        }
    }

    private Timestamp createAt;

    public Timestamp getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Object createAt) {
        if (createAt instanceof Timestamp) {
            this.createAt = (Timestamp) createAt;
        } else if (createAt instanceof String) {
            this.createAt = null; // or parse it
        }
    }

    public ManagerNotification() {} // Bắt buộc cho Firebase
}