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
import com.google.firebase.storage.FirebaseStorage;
import com.pkhh.projectcndd.R;
import com.pkhh.projectcndd.models.District;
import com.pkhh.projectcndd.models.FirebaseModel;
import com.pkhh.projectcndd.utils.RecyclerOnClickListener;

public class Activity_district extends AppCompatActivity implements RecyclerOnClickListener {
    public static final String ID_DIS = "ID_DIS";
    public static final String NAME_DIS = "NAME_DIS";
    private RecyclerView recyclerView_district;
    private FirestoreRecyclerAdapter<District, VH_dis> firestoreRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_district);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        recyclerView_district = findViewById(R.id.recycler_distrct);
        recyclerView_district.setHasFixedSize(true);
        recyclerView_district.setLayoutManager(new LinearLayoutManager(this));
        setupAdapter();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home )
            finish();
        return super.onOptionsItemSelected(item);

    }

    private void setupAdapter() {
        Intent intent_getid = getIntent();
        String id = intent_getid.getStringExtra("ID_PRO");
        Query query = FirebaseFirestore.getInstance().document("provinces" +"/" + id).collection("districts").orderBy("name");
        FirestoreRecyclerOptions<District> options = new FirestoreRecyclerOptions.Builder<District>().
                setQuery(query, snapshot -> FirebaseModel.documentSnapshotToObject(snapshot, District.class)).build();

        firestoreRecyclerAdapter = new FirestoreRecyclerAdapter<District, VH_dis>(options) {
            @Override
            protected void onBindViewHolder(@NonNull VH_dis vh_dis, int i, @NonNull District district) {
                vh_dis.bind(district);
            }

            @NonNull
            @Override
            public VH_dis onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new VH_dis(getLayoutInflater().inflate(R.layout.district_itemlayout, parent, false), Activity_district.this);
            }
        };
        recyclerView_district.setAdapter(firestoreRecyclerAdapter);
        firestoreRecyclerAdapter.startListening();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        firestoreRecyclerAdapter.stopListening();
    }

    @Override
    public void onClick(@NonNull View view, int position) {
        Intent intent= new Intent();
        intent.putExtra(ID_DIS, firestoreRecyclerAdapter.getItem(position).id);
        intent.putExtra(NAME_DIS, firestoreRecyclerAdapter.getItem(position).name);
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

class VH_dis extends RecyclerView.ViewHolder implements View.OnClickListener {

    private final TextView textView;
    private RecyclerOnClickListener recyclerOnClickListener;


    public VH_dis(@NonNull View itemView, RecyclerOnClickListener recyclerOnClickListener) {
        super(itemView);
        textView = itemView.findViewById(R.id.text_district_name);
        itemView.setOnClickListener(this);
        this.recyclerOnClickListener = recyclerOnClickListener;
    }

    public void bind(District district) {
        textView.setText(district.name);
    }

    @Override
    public void onClick(View v) {
        int adapterPosition = getAdapterPosition();
        if (adapterPosition != RecyclerView.NO_POSITION) {
            recyclerOnClickListener.onClick(v, adapterPosition);
        }
    }
}
