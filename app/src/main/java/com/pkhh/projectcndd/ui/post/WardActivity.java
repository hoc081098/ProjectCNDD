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
import com.pkhh.projectcndd.models.FirebaseModel;
import com.pkhh.projectcndd.models.Ward;
import com.pkhh.projectcndd.utils.RecyclerOnClickListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static java.util.Objects.requireNonNull;

public class WardActivity extends AppCompatActivity implements RecyclerOnClickListener {
  private RecyclerView mRecyclerViewWards;
  private FirestoreRecyclerAdapter<Ward, WardViewHolder> mFirestoreRecyclerAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_ward);
    requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

    mRecyclerViewWards = findViewById(R.id.recycler_ward);
    mRecyclerViewWards.setHasFixedSize(true);
    mRecyclerViewWards.setLayoutManager(new LinearLayoutManager(this));
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
    Intent intent = getIntent();
    String districtId = intent.getStringExtra(SelectLocationFragment.EXTRA_DISTRICT_ID);
    String provinceId = intent.getStringExtra(SelectLocationFragment.EXTRA_PROVINCE_ID);

    Query query = FirebaseFirestore.getInstance()
        .document("provinces" + "/" + provinceId + "/" + "districts" + "/" + districtId)
        .collection("wards")
        .orderBy("name");

    FirestoreRecyclerOptions<Ward> options = new FirestoreRecyclerOptions.Builder<Ward>().
        setQuery(query, snapshot -> FirebaseModel.documentSnapshotToObject(snapshot, Ward.class)).build();

    mFirestoreRecyclerAdapter = new FirestoreRecyclerAdapter<Ward, WardViewHolder>(options) {
      @Override
      protected void onBindViewHolder(@NonNull WardViewHolder viewHolder, int i, @NonNull Ward ward) {
        viewHolder.bind(ward);
      }

      @NonNull
      @Override
      public WardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new WardViewHolder(getLayoutInflater().inflate(R.layout.ward_item_layout, parent, false), WardActivity.this);
      }
    };
    mRecyclerViewWards.setAdapter(mFirestoreRecyclerAdapter);
    mFirestoreRecyclerAdapter.startListening();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mFirestoreRecyclerAdapter.stopListening();
  }

  @Override
  public void onClick(@NonNull View view, int position) {
    Ward item = mFirestoreRecyclerAdapter.getItem(position);

    Intent intent = new Intent();
    intent.putExtra(SelectLocationFragment.EXTRA_WARD_NAME, item.getName());
    intent.putExtra(SelectLocationFragment.EXTRA_WARD_ID, item.getId());

    setResult(Activity.RESULT_OK, intent);
    finish();
  }

  @Override
  public void onBackPressed() {
    // truyen du lieu that bai khi ng ducng click balck
    setResult(Activity.RESULT_CANCELED);
    super.onBackPressed();
  }

}

class WardViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

  private final TextView mTextView;
  private final RecyclerOnClickListener mRecyclerOnClickListener;


  public WardViewHolder(@NonNull View itemView, RecyclerOnClickListener recyclerOnClickListener) {
    super(itemView);
    mTextView = itemView.findViewById(R.id.text_ward_name);
    itemView.setOnClickListener(this);
    this.mRecyclerOnClickListener = recyclerOnClickListener;
  }

  public void bind(Ward ward) {
    mTextView.setText(ward.getName());
  }

  @Override
  public void onClick(View v) {
    int adapterPosition = getAdapterPosition();
    if (adapterPosition != RecyclerView.NO_POSITION) {
      mRecyclerOnClickListener.onClick(v, adapterPosition);
    }
  }
}
