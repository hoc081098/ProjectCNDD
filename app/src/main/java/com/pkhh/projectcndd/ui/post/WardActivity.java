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
import com.pkhh.projectcndd.models.Ward;
import com.pkhh.projectcndd.utils.RecyclerOnClickListener;

import static com.pkhh.projectcndd.ui.post.Activity_district.ID_DIS;
import static com.pkhh.projectcndd.ui.post.Fragment2.ID_PRO;

public class WardActivity extends AppCompatActivity implements RecyclerOnClickListener {
    public static final String NAME_WARD = "NAME_WARD";
    private RecyclerView recyclerView_ward;
    private FirestoreRecyclerAdapter<Ward, VHWard> firestoreRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ward);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        recyclerView_ward = findViewById(R.id.recycler_ward);
        recyclerView_ward.setHasFixedSize(true);
        recyclerView_ward.setLayoutManager(new LinearLayoutManager(this));
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
        String iddis = intent_getid.getStringExtra(ID_DIS);
        String idpro = intent_getid.getStringExtra(ID_PRO);
        Query query = FirebaseFirestore.getInstance().document("provinces" +"/" + idpro + "/" + "districts"+ "/" + iddis).collection("wards").orderBy("name");
        FirestoreRecyclerOptions<Ward> options = new FirestoreRecyclerOptions.Builder<Ward>().
                setQuery(query, snapshot -> FirebaseModel.documentSnapshotToObject(snapshot, Ward.class)).build();

        firestoreRecyclerAdapter = new FirestoreRecyclerAdapter<Ward, VHWard>(options) {
            @Override
            protected void onBindViewHolder(@NonNull VHWard vhWard, int i, @NonNull Ward ward) {
                vhWard.bind(ward);
            }

            @NonNull
            @Override
            public VHWard onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new VHWard(getLayoutInflater().inflate(R.layout.ward_item_layout, parent, false), WardActivity.this);
            }
        };
        recyclerView_ward.setAdapter(firestoreRecyclerAdapter);
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
        intent.putExtra(NAME_WARD, firestoreRecyclerAdapter.getItem(position).name);
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

class VHWard extends RecyclerView.ViewHolder implements View.OnClickListener {

    private final TextView textView;
    private RecyclerOnClickListener recyclerOnClickListener;


    public VHWard(@NonNull View itemView, RecyclerOnClickListener recyclerOnClickListener) {
        super(itemView);
        textView = itemView.findViewById(R.id.text_ward_name);
        itemView.setOnClickListener(this);
        this.recyclerOnClickListener = recyclerOnClickListener;
    }

    public void bind(Ward ward) {
        textView.setText(ward.name);
    }

    @Override
    public void onClick(View v) {
        int adapterPosition = getAdapterPosition();
        if (adapterPosition != RecyclerView.NO_POSITION) {
            recyclerOnClickListener.onClick(v, adapterPosition);
        }
    }
}
