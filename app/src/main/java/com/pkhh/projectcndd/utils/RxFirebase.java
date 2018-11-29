package com.pkhh.projectcndd.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import io.reactivex.Observable;

public class RxFirebase {
  public static Observable<FirebaseAuth> authStateChanges(FirebaseAuth auth) {
    return Observable.create(emitter -> {
      final FirebaseAuth.AuthStateListener authStateListener = firebaseAuth -> {
        if (!emitter.isDisposed()) {
          emitter.onNext(firebaseAuth);
        }
      };
      auth.addAuthStateListener(authStateListener);
      emitter.setCancellable(() -> auth.removeAuthStateListener(authStateListener));
    });
  }


  public static Observable<DocumentSnapshot> documentSnapshots(DocumentReference documentReference) {
    return Observable.create(emitter -> {
      final ListenerRegistration registration = documentReference.addSnapshotListener((documentSnapshot, e) -> {
        if (e != null || documentReference == null) {
          return;
        }
        if (!emitter.isDisposed()) {
          emitter.onNext(documentSnapshot);
        }
      });
      emitter.setCancellable(registration::remove);
    });
  }
}
