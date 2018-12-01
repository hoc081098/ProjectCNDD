package com.pkhh.projectcndd.screen.posted;

import android.os.Bundle;
import android.view.MenuItem;

import com.pkhh.projectcndd.R;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import static java.util.Objects.requireNonNull;

public class PostedRoomsActivity extends AppCompatActivity {
  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_posted_room);

    requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    requireNonNull(getSupportActionBar()).setTitle(R.string.posted_rooms);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      onBackPressed();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }
}
