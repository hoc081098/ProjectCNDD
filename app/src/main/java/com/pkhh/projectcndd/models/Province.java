package com.pkhh.projectcndd.models;

import com.google.firebase.firestore.PropertyName;

import java.util.Date;
import java.util.Objects;

public class Province extends FirebaseModel {
  private String name;

  private Date createdAt;

  private Date updatedAt;

  public Province() {
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @PropertyName("created_at")
  public Date getCreatedAt() {
    return createdAt;
  }

  @PropertyName("created_at")
  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }


  @PropertyName("updated_at")
  public Date getUpdatedAt() {
    return updatedAt;
  }


  @PropertyName("updated_at")
  public void setUpdatedAt(Date updatedAt) {
    this.updatedAt = updatedAt;
  }

  @Override
  public String toString() {
    return "Province{" +
        "name='" + name + '\'' +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        ", id='" + id + '\'' +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Province province = (Province) o;
    return Objects.equals(id, province.id) &&
        Objects.equals(name, province.name) &&
        Objects.equals(createdAt, province.createdAt) &&
        Objects.equals(updatedAt, province.updatedAt);
  }

  @Override
  public int hashCode() {

    return Objects.hash(name, createdAt, updatedAt);
  }
}
