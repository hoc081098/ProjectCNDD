package com.pkhh.projectcndd.models;

import com.google.firebase.firestore.PropertyName;

import java.util.Date;
import java.util.Objects;

public class User extends FirebaseModel {
  private String email;
  private String phone;
  private String fullName;
  private String address;
  private String avatar;
  private boolean isActive;
  private Date createdAt;

  public User() {
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }


  @PropertyName("full_name")
  public String getFullName() {
    return fullName;
  }


  @PropertyName("full_name")
  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getAvatar() {
    return avatar;
  }

  public void setAvatar(String avatar) {
    this.avatar = avatar;
  }

  @PropertyName("is_active")
  public boolean isActive() {
    return isActive;
  }

  @PropertyName("is_active")
  public void setActive(boolean active) {
    isActive = active;
  }

  @PropertyName("created_at")
  public Date getCreatedAt() {
    return createdAt;
  }

  @PropertyName("created_at")
  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }

  @Override
  public String toString() {
    return "User{" +
        "email='" + email + '\'' +
        ", phone='" + phone + '\'' +
        ", fullName='" + fullName + '\'' +
        ", address='" + address + '\'' +
        ", avatar='" + avatar + '\'' +
        ", isAvailable=" + isActive +
        ", createdAt=" + createdAt +
        ", id='" + id + '\'' +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    User user = (User) o;
    return Objects.equals(id, user.id) &&
        isActive == user.isActive &&
        Objects.equals(email, user.email) &&
        Objects.equals(phone, user.phone) &&
        Objects.equals(fullName, user.fullName) &&
        Objects.equals(address, user.address) &&
        Objects.equals(avatar, user.avatar) &&
        Objects.equals(createdAt, user.createdAt);
  }

  @Override
  public int hashCode() {

    return Objects.hash(email, phone, fullName, address, avatar, isActive, createdAt);
  }
}
