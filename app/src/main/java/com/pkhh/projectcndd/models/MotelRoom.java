package com.pkhh.projectcndd.models;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.PropertyName;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;

public class MotelRoom extends FirebaseModel {
    private String title;

    private String description;

    private long price;

    private long countView;

    private double size;

    private String address;

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

    private Date createdAt;

    private Date updatedAt;

    private List<String> userIdsSaved;

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

    @PropertyName("count_view")
    public long getCountView() {
        return countView;
    }

    public double getSize() {
        return size;
    }

    public String getAddress() {
        return address;
    }

    @PropertyName("address_geopoint")
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

    @PropertyName("created_at")
    public Date getCreatedAt() {
        return createdAt;
    }

    @PropertyName("updated_at")
    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    @PropertyName("count_view")
    public void setCountView(long countView) {
        this.countView = countView;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @PropertyName("address_geopoint")
    public void setAddressGeoPoint(GeoPoint addressGeoPoint) {
        this.addressGeoPoint = addressGeoPoint;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setOwner(boolean owner) {
        this.owner = owner;
    }

    public void setApprove(boolean approve) {
        this.approve = approve;
    }

    public void setUtilities(Map<String, Object> utilities) {
        this.utilities = utilities;
    }

    public void setUser(DocumentReference user) {
        this.user = user;
    }

    public void setCategory(DocumentReference category) {
        this.category = category;
    }

    public void setProvinces(DocumentReference provinces) {
        this.provinces = provinces;
    }

    public void setWard(DocumentReference ward) {
        this.ward = ward;
    }

    public void setDistrict(DocumentReference district) {
        this.district = district;
    }

    @PropertyName("created_at")
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @PropertyName("updated_at")
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    @NonNull
    @PropertyName("user_ids_saved")
    public List<String> getUserIdsSaved() {
        return userIdsSaved == null ? Collections.emptyList() : userIdsSaved;
    }

    @PropertyName("user_ids_saved")
    public void setUserIdsSaved(List<String> userIdsSaved) {
        this.userIdsSaved = userIdsSaved;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MotelRoom motelRoom = (MotelRoom) o;
        return Objects.equals(id, motelRoom.id) &&
                price == motelRoom.price &&
                countView == motelRoom.countView &&
                Double.compare(motelRoom.size, size) == 0 &&
                owner == motelRoom.owner &&
                approve == motelRoom.approve &&
                Objects.equals(title, motelRoom.title) &&
                Objects.equals(description, motelRoom.description) &&
                Objects.equals(address, motelRoom.address) &&
                Objects.equals(addressGeoPoint, motelRoom.addressGeoPoint) &&
                Objects.equals(images, motelRoom.images) &&
                Objects.equals(phone, motelRoom.phone) &&
                Objects.equals(utilities, motelRoom.utilities) &&
                Objects.equals(user, motelRoom.user) &&
                Objects.equals(category, motelRoom.category) &&
                Objects.equals(provinces, motelRoom.provinces) &&
                Objects.equals(ward, motelRoom.ward) &&
                Objects.equals(district, motelRoom.district) &&
                Objects.equals(createdAt, motelRoom.createdAt) &&
                Objects.equals(updatedAt, motelRoom.updatedAt) &&
                Objects.equals(userIdsSaved, motelRoom.userIdsSaved);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, price, countView, size, address, addressGeoPoint,
                images, phone, owner, approve, utilities, user, category, provinces, ward, district, createdAt, updatedAt, userIdsSaved);
    }

    @Override
    public String toString() {
        return "MotelRoom{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", countView=" + countView +
                ", size=" + size +
                ", address='" + address + '\'' +
                ", addressGeoPoint=" + addressGeoPoint +
                ", images=" + images +
                ", phone='" + phone + '\'' +
                ", owner=" + owner +
                ", approve=" + approve +
                ", utilities=" + utilities +
                ", user=" + user +
                ", category=" + category +
                ", provinces=" + provinces +
                ", ward=" + ward +
                ", district=" + district +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", userIdsSaved=" + userIdsSaved +
                ", id='" + id + '\'' +
                '}';
    }
}