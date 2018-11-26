package com.pkhh.projectcndd;

import android.app.Application;

import com.google.firebase.firestore.FirebaseFirestore;

import androidx.multidex.MultiDexApplication;
import timber.log.Timber;

public class App extends MultiDexApplication {
  @Override
  public void onCreate() {
    super.onCreate();
    if (BuildConfig.DEBUG) {
      Timber.plant(new Timber.DebugTree());
      FirebaseFirestore.setLoggingEnabled(true);
    }
  }
}
