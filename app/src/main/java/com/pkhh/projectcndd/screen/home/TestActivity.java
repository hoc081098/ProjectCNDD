package com.pkhh.projectcndd.screen.home;

import android.os.Bundle;
import android.widget.Toast;

import com.pkhh.projectcndd.R;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class TestActivity extends AppCompatActivity {
  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_test);

    Toast.makeText(this, getIntent().getIntExtra(HomeAdapter.QUERY_DIRECTION, 0) + "", Toast.LENGTH_SHORT).show();
  }
}
