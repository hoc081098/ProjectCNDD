package com.pkhh.projectcndd.models;

import com.annimon.stream.Stream;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

import androidx.annotation.NonNull;

import static java.util.Objects.requireNonNull;

public abstract class FirebaseModel {
  @Exclude protected String id;

  @NonNull
  public static <T extends FirebaseModel> T documentSnapshotToObject(@NonNull DocumentSnapshot snapshot, Class<T> tClass) {
    T t = requireNonNull(snapshot.toObject(tClass), "FirebaseModel::documentSnapshotToObject");
    t.id = snapshot.getId();
    return t;
  }

  @NonNull
  public static <T extends FirebaseModel> List<T> querySnapshotToObjects(@NonNull QuerySnapshot snapshot, Class<T> tClass) {
    return Stream.of(snapshot.getDocuments())
        .map(doc -> documentSnapshotToObject(doc, tClass))
        .toList();
  }

  @Exclude
  @NonNull
  public String getId() {
    return requireNonNull(id, "Id must be initialized");
  }
}
