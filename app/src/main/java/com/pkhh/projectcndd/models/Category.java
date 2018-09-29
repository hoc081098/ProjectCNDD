package com.pkhh.projectcndd.models;

import com.google.firebase.firestore.PropertyName;

import java.util.Date;
import java.util.Objects;

// done
public class Category extends FirebaseModel {
    private String name;

    @PropertyName("created_at")
    private Date createdAt;

    @PropertyName("updated_at")
    private Date updatedAt;

    public Category() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Category)) return false;
        Category category = (Category) o;
        return Objects.equals(name, category.name) &&
                Objects.equals(createdAt, category.createdAt) &&
                Objects.equals(updatedAt, category.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, createdAt, updatedAt);
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
