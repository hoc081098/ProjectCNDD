package com.pkhh.projectcndd.models;

import com.google.firebase.firestore.PropertyName;

import java.util.Date;

public class District extends FirebaseModel {
    private String name;

    @PropertyName("created_at")
    private Date createdAt;

    @PropertyName("updated_at")
    private Date updatedAt;

    public District() {
    }

    public String getName() {
        return name;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }
}
