package com.pkhh.projectcndd.models;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;

public abstract class FirebaseModel {
    public String id;

    @NonNull
    public static <T extends FirebaseModel> T documentSnapshotToObject(@NonNull DocumentSnapshot snapshot, Class<T> tClass) {
        T t = Objects.requireNonNull(snapshot.toObject(tClass));
        t.id = snapshot.getId();
        return t;
    }

    @NonNull
    public static <T extends FirebaseModel> List<T> querySnapshotToObjects(@NonNull QuerySnapshot snapshot, Class<T> tClass) {
        List<T> list = new ArrayList<>(snapshot.size());
        for (DocumentSnapshot documentSnapshot : snapshot.getDocuments()) {
            list.add(documentSnapshotToObject(documentSnapshot, tClass));
        }
        return list;
    }
}
