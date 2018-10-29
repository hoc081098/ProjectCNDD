package com.pkhh.projectcndd.models;

import com.google.firebase.firestore.PropertyName;

import java.util.Date;
import java.util.Objects;

public class Category extends FirebaseModel {
  private String name;

  private Date createdAt;

  private Date updatedAt;

  public Category() {
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Category category = (Category) o;
    return Objects.equals(id, category.id) &&
        Objects.equals(name, category.name) &&
        Objects.equals(createdAt, category.createdAt) &&
        Objects.equals(updatedAt, category.updatedAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, createdAt, updatedAt);
  }

  @Override
  public String toString() {
    return "Category{" +
        "name='" + name + '\'' +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        ", id='" + id + '\'' +
        '}';
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
}
