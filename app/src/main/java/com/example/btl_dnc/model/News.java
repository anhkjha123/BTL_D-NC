package com.example.btl_dnc.model;

import com.google.firebase.Timestamp;

public class News {
    public String id;
    public String authorID;
    public String content;
    private Timestamp createAt;
    private Timestamp updateAt;
    public String imageBase64;
    public String title;

    public Timestamp getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Object createAt) {
        if (createAt instanceof Timestamp) {
            this.createAt = (Timestamp) createAt;
        } else if (createAt instanceof String) {
            // Optionally parse the string if needed, otherwise just ignore to avoid crash
            this.createAt = null; 
        }
    }

    public Timestamp getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(Object updateAt) {
        if (updateAt instanceof Timestamp) {
            this.updateAt = (Timestamp) updateAt;
        } else if (updateAt instanceof String) {
            this.updateAt = null;
        }
    }

    public News() {}
}
