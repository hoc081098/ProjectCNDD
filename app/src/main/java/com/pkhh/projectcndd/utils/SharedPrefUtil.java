package com.pkhh.projectcndd.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class SharedPrefUtil {
  public static final String SELECTED_PROVINCE_ID_KEY = "com.pkhh.projectcndd.selected_city_id";

  private volatile static SharedPrefUtil sInstance = null;
  private SharedPreferences sharedPreferences;

  private SharedPrefUtil() {}

  private SharedPrefUtil(Context context) {
    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
  }

  public void saveSelectedProvinceId(@Nullable String id) {
    sharedPreferences.edit().putString(SELECTED_PROVINCE_ID_KEY, id).apply();
  }

  @Nullable
  public String getSelectedProvinceId() {
    return sharedPreferences.getString(SELECTED_PROVINCE_ID_KEY, null);
  }

  @NonNull
  public static SharedPrefUtil getInstance(Context context) {
    if (sInstance == null) {
      synchronized (SharedPrefUtil.class) {
        if (sInstance == null) {
          sInstance = new SharedPrefUtil(context);
        }
      }
    }
    return sInstance;
  }
}
