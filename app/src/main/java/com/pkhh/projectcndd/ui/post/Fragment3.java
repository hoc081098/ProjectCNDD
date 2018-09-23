package com.pkhh.projectcndd.ui.post;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.pkhh.projectcndd.R;
import com.pkhh.projectcndd.utils.RecyclerOnClickListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

public class Fragment3 extends Fragment {
    private ConstraintLayout selectImage;
    private RecyclerView recyclerViewImage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_3, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        recyclerViewImage.setHasFixedSize(true);
        recyclerViewImage.setLayoutManager(new LinearLayoutManager(requireContext()));
        setupAdapter();
    }

    private void setupAdapter() {
        new ImageAdapter(new RecyclerOnClickListener() {
            @Override
            public void onClick(@NonNull View view, int position) {

            }
        });
    }

    private void initView(View view) {
        selectImage = view.findViewById(R.id.select_img);
        recyclerViewImage = view.findViewById(R.id.recycler_img);
    }
}

class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.VH> {

    private RecyclerOnClickListener onClick;

    ImageAdapter(RecyclerOnClickListener onClick) {
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_iteam_layout, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    class VH extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView imageViewPreview;
        private ImageView imageViewClose;

        public VH(@NonNull View itemView) {
            super(itemView);
            imageViewPreview = itemView.findViewById(R.id.image_preview);
            imageViewClose = itemView.findViewById(R.id.img_close);
            imageViewPreview.setOnClickListener(this);
            imageViewClose.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            if (adapterPosition != NO_POSITION) {
                onClick.onClick(v, adapterPosition);
            }
        }
    }
}
