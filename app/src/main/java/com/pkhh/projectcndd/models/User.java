package com.pkhh.projectcndd.models;

import com.google.firebase.firestore.PropertyName;

import java.util.Date;

public class User extends FirebaseModel {
    private String email;

    private String phone;

    @PropertyName("full_name")
    private String fullName;

    private String address;

    private String avatar;

    @PropertyName("is_active")
    private boolean isActive;

    @PropertyName("created_at")
    private Date createdAt;

    @PropertyName("updated_at")
    private Date updatedAt;

    public User() {
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getFullName() {
        return fullName;
    }

    public String getAddress() {
        return address;
    }

    public String getAvatar() {
        return avatar;
    }

    public boolean isActive() {
        return isActive;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }
}
