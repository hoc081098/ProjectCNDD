package com.pkhh.projectcndd.models;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.PropertyName;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class MotelRoom extends FirebaseModel {
    public String title;

    public String description;

    public long price;

    @PropertyName("count_view")
    public long countView;

    public double size;

    public String address;

    @PropertyName("address_geopoint")
    public GeoPoint addressGeoPoint;

    public List<String> images;

    public String phone;

    public boolean owner;

    public boolean approve;

    public Map<String, Object> utilities;

    public DocumentReference user;

    public DocumentReference category;

    public DocumentReference provinces;

    public DocumentReference ward;

    public DocumentReference district;

    @PropertyName("created_at")
    public Date createdAt;

    @PropertyName("created_at")
    public Date updatedAt;

    // Firebase Firestore require empty constructor
    public MotelRoom() {
    }
}