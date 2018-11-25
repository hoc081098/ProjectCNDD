package com.pkhh.projectcndd.screen;

import android.content.Intent;
import android.os.Bundle;

import com.pkhh.projectcndd.screen.home.MainActivity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SplashScreen extends AppCompatActivity {
  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    startActivity(new Intent(this, MainActivity.class));
    finish();
  }
}
