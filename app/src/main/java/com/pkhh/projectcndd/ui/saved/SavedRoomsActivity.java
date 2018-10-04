package com.pkhh.projectcndd.ui.saved;

import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.pkhh.projectcndd.R;
import com.pkhh.projectcndd.utils.Constants;

import java.util.Objects;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public final class SavedRoomsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_district);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        final RecyclerView recyclerViewSaved = findViewById(R.id.recycler_distrct);
        recyclerViewSaved.setHasFixedSize(true);
        recyclerViewSaved.setLayoutManager(new LinearLayoutManager(this));

        FirebaseFirestore.getInstance()
                .collection(Constants.USER_NAME_COLLECION + "/"
                        + FirebaseAuth.getInstance().getCurrentUser().getUid() + Constants.SAVED_ROOMS_NAME_COLLECTION)
                .orderBy("created_at", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {


                });
    }
}
