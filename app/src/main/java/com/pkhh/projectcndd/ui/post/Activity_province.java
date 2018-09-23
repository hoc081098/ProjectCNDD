package com.pkhh.projectcndd.ui.post;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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


public class Activity_province extends AppCompatActivity implements RecyclerOnClickListener {
    private RecyclerView recyclerView_provinces;
    private FirestoreRecyclerAdapter<Province, VH> firestoreRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_province);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        recyclerView_provinces = findViewById(R.id.recycler_provinces);
        recyclerView_provinces.setHasFixedSize(true);
        recyclerView_provinces.setLayoutManager(new LinearLayoutManager(this));
        setupAdaper();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home )
            finish();
        return super.onOptionsItemSelected(item);

    }

    private void setupAdaper() {
        Query query = FirebaseFirestore.getInstance().collection("provinces").orderBy("name");
        FirestoreRecyclerOptions<Province> options = new FirestoreRecyclerOptions.Builder<Province>()
                .setQuery(query, snapshot -> FirebaseModel.documentSnapshotToObject(snapshot, Province.class))
                .build();
        firestoreRecyclerAdapter = new FirestoreRecyclerAdapter<Province, VH>(options) {
            @NonNull
            @Override
            public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new VH(getLayoutInflater().inflate(R.layout.province_itemlayout, parent, false), Activity_province.this);
            }

            @Override
            protected void onBindViewHolder(@NonNull VH vh, int i, @NonNull Province province) {
                vh.bind(province);

            }
        };
        recyclerView_provinces.setAdapter(firestoreRecyclerAdapter);
        firestoreRecyclerAdapter.startListening();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        firestoreRecyclerAdapter.stopListening();
    }

    @Override
    public void onClick(@NonNull View view, int position) {
        Province item = firestoreRecyclerAdapter.getItem(position);
        String id = item.id;
        String name = item.name;
        Intent intent = new Intent();
        intent.putExtra("NAME_PRO", name);
        intent.putExtra("ID_PRO", id);
        setResult(Activity.RESULT_OK, intent);
        finish();                            // bat dau man hinh truoc
    }

    @Override
    public void onBackPressed() {
        // truyen du lieu that bai khi ng ducng click balck
        setResult(Activity.RESULT_CANCELED);
        super.onBackPressed();
    }
}

class VH extends RecyclerView.ViewHolder implements View.OnClickListener {

    private final TextView view;
    private RecyclerOnClickListener recyclerOnClickListener;

    public VH(@NonNull View itemView, RecyclerOnClickListener recyclerOnClickListener) {
        super(itemView);
        view = itemView.findViewById(R.id.text_province_name);
        itemView.setOnClickListener(this);
        this.recyclerOnClickListener = recyclerOnClickListener;
    }

    public void bind(Province province) {
        view.setText(province.name);
    }

    @Override
    public void onClick(View v) {
        int adapterPosition = getAdapterPosition();
        if (adapterPosition != RecyclerView.NO_POSITION) {
            recyclerOnClickListener.onClick(v, adapterPosition);
        }
    }
}
