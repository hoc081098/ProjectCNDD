package com.pkhh.projectcndd.models;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.PropertyName;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class MotelRoom extends FirebaseModel {
    private String title;

    private String description;

    private long price;

    @PropertyName("count_view")
    private long countView;

    private double size;

    private String address;

    @PropertyName("address_geopoint")
    private GeoPoint addressGeoPoint;

    private List<String> images;

    private String phone;

    private boolean owner;

    private boolean approve;

    private Map<String, Object> utilities;

    private DocumentReference user;

    private DocumentReference category;

    private DocumentReference provinces;

    private DocumentReference ward;

    private DocumentReference district;

    @PropertyName("created_at")
    private Date createdAt;

    @PropertyName("created_at")
    private Date updatedAt;

    // Firebase Firestore require empty constructor
    public MotelRoom() {
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public long getPrice() {
        return price;
    }

    public long getCountView() {
        return countView;
    }

    public double getSize() {
        return size;
    }

    public String getAddress() {
        return address;
    }

    public GeoPoint getAddressGeoPoint() {
        return addressGeoPoint;
    }

    public List<String> getImages() {
        return images;
    }

    public String getPhone() {
        return phone;
    }

    public boolean isOwner() {
        return owner;
    }

    public boolean isApprove() {
        return approve;
    }

    public Map<String, Object> getUtilities() {
        return utilities;
    }

    public DocumentReference getUser() {
        return user;
    }

    public DocumentReference getCategory() {
        return category;
    }

    public DocumentReference getProvinces() {
        return provinces;
    }

    public DocumentReference getWard() {
        return ward;
    }

    public DocumentReference getDistrict() {
        return district;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }
}