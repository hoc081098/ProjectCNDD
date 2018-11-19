package com.pkhh.projectcndd.screen.post;

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
import com.pkhh.projectcndd.models.Province;
import com.pkhh.projectcndd.utils.RecyclerOnClickListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static java.util.Objects.requireNonNull;


public class ProvinceActivity extends AppCompatActivity implements RecyclerOnClickListener {
  private RecyclerView mRecyclerviewProvinces;
  private FirestoreRecyclerAdapter<Province, ProvinceViewHolder> mFirestoreRecyclerAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_province);
    requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

    mRecyclerviewProvinces = findViewById(R.id.recycler_provinces);
    mRecyclerviewProvinces.setHasFixedSize(true);
    mRecyclerviewProvinces.setLayoutManager(new LinearLayoutManager(this));
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
    Query query = FirebaseFirestore.getInstance()
        .collection("provinces")
        .orderBy("name");

    FirestoreRecyclerOptions<Province> options = new FirestoreRecyclerOptions.Builder<Province>()
        .setQuery(query, snapshot -> FirebaseModel.documentSnapshotToObject(snapshot, Province.class))
        .build();

    mFirestoreRecyclerAdapter = new FirestoreRecyclerAdapter<Province, ProvinceViewHolder>(options) {
      @NonNull
      @Override
      public ProvinceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ProvinceViewHolder(getLayoutInflater().inflate(R.layout.province_item_layout, parent, false), ProvinceActivity.this);
      }

      @Override
      protected void onBindViewHolder(@NonNull ProvinceViewHolder viewHolder, int i, @NonNull Province province) {
        viewHolder.bind(province);

      }
    };
    mRecyclerviewProvinces.setAdapter(mFirestoreRecyclerAdapter);
    mFirestoreRecyclerAdapter.startListening();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mFirestoreRecyclerAdapter.stopListening();
  }

  @Override
  public void onClick(@NonNull View view, int position) {
    Province item = mFirestoreRecyclerAdapter.getItem(position);
    String id = item.getId();
    String name = item.getName();

    Intent intent = new Intent();
    intent.putExtra(SelectAddressLocationFragment.EXTRA_PROVINCE_NAME, name);
    intent.putExtra(SelectAddressLocationFragment.EXTRA_PROVINCE_ID, id);

    setResult(Activity.RESULT_OK, intent);
    finish(); // quay dau man hinh truoc
  }

  @Override
  public void onBackPressed() {
    // truyen du lieu that bai khi ng ducng click back
    setResult(Activity.RESULT_CANCELED);
    super.onBackPressed();
  }
}

class ProvinceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
  private final TextView mTextView;
  private final RecyclerOnClickListener mRecyclerOnClickListener;

  public ProvinceViewHolder(@NonNull View itemView, RecyclerOnClickListener recyclerOnClickListener) {
    super(itemView);
    mTextView = itemView.findViewById(R.id.text_province_name);
    itemView.setOnClickListener(this);
    this.mRecyclerOnClickListener = recyclerOnClickListener;
  }

  public void bind(Province province) {
    mTextView.setText(province.getName());
  }

  @Override
  public void onClick(View v) {
    int adapterPosition = getAdapterPosition();
    if (adapterPosition != RecyclerView.NO_POSITION) {
      mRecyclerOnClickListener.onClick(v, adapterPosition);
    }
  }
}
