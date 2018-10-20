package com.pkhh.projectcndd.ui.post;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.pkhh.projectcndd.R;
import com.pkhh.projectcndd.models.District;
import com.pkhh.projectcndd.utils.RecyclerOnClickListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.pkhh.projectcndd.models.FirebaseModel.documentSnapshotToObject;
import static java.util.Objects.requireNonNull;

public class DistrictActivity extends AppCompatActivity implements RecyclerOnClickListener {
  private RecyclerView mRecyclerViewDistrict;
  private FirestoreRecyclerAdapter<District, DistrictViewHolder> mFirestoreRecyclerAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_district);
    requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

    mRecyclerViewDistrict = findViewById(R.id.recycler_distrct);
    mRecyclerViewDistrict.setHasFixedSize(true);
    mRecyclerViewDistrict.setLayoutManager(new LinearLayoutManager(this));
    setupAdapter();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      finish();
    }
    return super.onOptionsItemSelected(item);

  }

  private void setupAdapter() {
    String id = getIntent().getStringExtra(SelectLocationFragment.EXTRA_PROVINCE_ID);
    Query query = FirebaseFirestore.getInstance()
        .document("provinces" + "/" + id)
        .collection("districts")
        .orderBy("name");

    FirestoreRecyclerOptions<District> options = new FirestoreRecyclerOptions.Builder<District>()
        .setQuery(query, snapshot -> documentSnapshotToObject(snapshot, District.class))
        .build();

    mFirestoreRecyclerAdapter = new FirestoreRecyclerAdapter<District, DistrictViewHolder>(options) {
      @Override
      protected void onBindViewHolder(@NonNull DistrictViewHolder viewHolder, int i, @NonNull District district) {
        viewHolder.bind(district);
      }

      @NonNull
      @Override
      public DistrictViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DistrictViewHolder(getLayoutInflater().inflate(R.layout.district_item_layout, parent, false), DistrictActivity.this);
      }
    };
    mRecyclerViewDistrict.setAdapter(mFirestoreRecyclerAdapter);
    mFirestoreRecyclerAdapter.startListening();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mFirestoreRecyclerAdapter.stopListening();
  }

  @Override
  public void onClick(@NonNull View view, int position) {
    District item = mFirestoreRecyclerAdapter.getItem(position);

    Intent intent = new Intent();
    intent.putExtra(SelectLocationFragment.EXTRA_DISTRICT_ID, item.getId());
    intent.putExtra(SelectLocationFragment.EXTRA_DISTRICT_NAME, item.getName());

    setResult(Activity.RESULT_OK, intent);
    finish();
  }

  @Override
  public void onBackPressed() {
    // truyen du lieu that bai khi ng ducng click back
    setResult(Activity.RESULT_CANCELED);
    super.onBackPressed();
  }

}

class DistrictViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
  private final TextView mTextView;
  private final RecyclerOnClickListener mRecyclerOnClickListener;


  public DistrictViewHolder(@NonNull View itemView, RecyclerOnClickListener recyclerOnClickListener) {
    super(itemView);
    mTextView = itemView.findViewById(R.id.text_district_name);
    itemView.setOnClickListener(this);
    this.mRecyclerOnClickListener = recyclerOnClickListener;
  }

  public void bind(District district) {
    mTextView.setText(district.getName());
  }

  @Override
  public void onClick(View v) {
    int adapterPosition = getAdapterPosition();
    if (adapterPosition != RecyclerView.NO_POSITION) {
      mRecyclerOnClickListener.onClick(v, adapterPosition);
    }
  }
}
