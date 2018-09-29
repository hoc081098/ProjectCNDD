package com.pkhh.projectcndd.models;

import com.annimon.stream.Stream;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

import androidx.annotation.NonNull;

import static java.util.Objects.requireNonNull;

public abstract class FirebaseModel {
    protected String id;

    @NonNull
    public String getId() {
        return requireNonNull(id, "Id must be initialize");
    }

    @NonNull
    public static <T extends FirebaseModel> T documentSnapshotToObject(@NonNull DocumentSnapshot snapshot, Class<T> tClass) {
        T t = requireNonNull(snapshot.toObject(tClass), "FirebaseModel::documentSnapshotToObject");
        t.id = snapshot.getId();
        return t;
    }

    @NonNull
    public static <T extends FirebaseModel> List<T> querySnapshotToObjects(@NonNull QuerySnapshot snapshot, Class<T> tClass) {
        return Stream.of(snapshot.getDocuments()) // Stream<DocumentSnapshot>
                .map(doc -> documentSnapshotToObject(doc, tClass)) // Stream<FirebaseModel>
                .toList(); // List<FirebaseModel>

//        List<T> list = new ArrayList<>(snapshot.size());
//        for (DocumentSnapshot documentSnapshot : snapshot.getDocuments()) {
//            list.add(documentSnapshotToObject(documentSnapshot, tClass));
//        }
//        return list;
    }
}
