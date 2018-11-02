package com.pkhh.projectcndd.ui.home;

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

    Toast.makeText(this, getIntent().getSerializableExtra(HomeAdapter.QUERY_DIRECTION).toString()
        , Toast.LENGTH_SHORT).show();
  }
}
