package com.pkhh.projectcndd.ui.post;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.pkhh.projectcndd.R;
import com.pkhh.projectcndd.models.Category;
import com.pkhh.projectcndd.utils.RecyclerOnClickListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;
import static com.pkhh.projectcndd.models.FirebaseModel.documentSnapshotToObject;
import static com.pkhh.projectcndd.utils.Constants.CATEGORY_NAME_COLLECION;

public class SelectCategoryFragment extends Fragment implements RecyclerOnClickListener {
    private RecyclerView recyclerView;
    private FirestoreRecyclerAdapter<Category, ViewHolder> adapter;
    private Category selected;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_select_category, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recycler_category);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));

        setupAdapter();
    }

    private void setupAdapter() {
        Query query = FirebaseFirestore.getInstance().collection(CATEGORY_NAME_COLLECION);
        FirestoreRecyclerOptions<Category> options = new FirestoreRecyclerOptions.Builder<Category>()
                .setQuery(query, snapshot -> documentSnapshotToObject(snapshot, Category.class))
                .build();
        adapter = new FirestoreRecyclerAdapter<Category, ViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ViewHolder viewHolder, int i, @NonNull Category category) {
                viewHolder.bind(category, selected);
            }

            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                final View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_item_layout, parent, false);
                return new ViewHolder(itemView, SelectCategoryFragment.this);
            }
        };
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    @Nullable
    public Category getSelectedCategory() {
        return selected;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        adapter.stopListening();
    }

    @Override
    public void onClick(@NonNull View view, int position) {
        selected = adapter.getItem(position);
        adapter.notifyDataSetChanged();
    }

    private static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final RadioButton radioButton;
        private final TextView textCategoryName;
        private final RecyclerOnClickListener recyclerOnClickListener;

        ViewHolder(@NonNull View itemView, @NonNull RecyclerOnClickListener onClickListener) {
            super(itemView);

            recyclerOnClickListener = onClickListener;

            radioButton = itemView.findViewById(R.id.radio_button);
            textCategoryName = itemView.findViewById(R.id.text_category_name);

            itemView.setOnClickListener(this);
        }

        void bind(Category category, @Nullable Category selected) {
            if (selected != null) {
                radioButton.setChecked(category.id.equals(selected.id));
            }
            textCategoryName.setText(category.name);
        }

        @Override
        public void onClick(View v) {
            final int adapterPosition = getAdapterPosition();
            if (adapterPosition != NO_POSITION) {
                recyclerOnClickListener.onClick(v, adapterPosition);
            }
        }
    }
}
