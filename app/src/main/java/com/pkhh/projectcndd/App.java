package com.pkhh.projectcndd;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import com.google.firebase.firestore.FirebaseFirestore;
import com.mapbox.mapboxsdk.Mapbox;

import androidx.multidex.MultiDexApplication;
import timber.log.Timber;

import static java.util.Objects.requireNonNull;

public class App extends MultiDexApplication {
  public static final String CHANNEL_ID = "com.pkhh.projectcndd";

  @Override
  public void onCreate() {
    super.onCreate();

    // Mapbox Access token
    Mapbox.getInstance(this, getString(R.string.mapbox_access_token));

    // Create notification channel
    createNotificationChannel();

    if (BuildConfig.DEBUG) {
      Timber.plant(new Timber.DebugTree());
      FirebaseFirestore.setLoggingEnabled(true);
    }
  }

  private void createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      final NotificationChannel channel = new NotificationChannel(
          CHANNEL_ID,
          getString(R.string.app_name),
          NotificationManager.IMPORTANCE_HIGH
      );
      channel.setDescription(getString(R.string.notification_channel_description, getString(R.string.app_name)));
      ((NotificationManager) requireNonNull(getSystemService(Context.NOTIFICATION_SERVICE))).createNotificationChannel(channel);
    }
  }

}
