package com.pkhh.projectcndd.ui.post;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.pkhh.projectcndd.R;
import com.pkhh.projectcndd.utils.RecyclerOnClickListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

public class AddPhotoFragment extends Fragment implements RecyclerOnClickListener, View.OnClickListener {
    public static final int REQUEST_CODE_SELECT_IMAGE = 1;
    private ConstraintLayout mSelectImage;
    private RecyclerView mRecyclerViewImages;
    private ImageAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_photo, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);

        mSelectImage.setOnClickListener(this);

        mRecyclerViewImages.setHasFixedSize(true);
        mRecyclerViewImages.setLayoutManager(new LinearLayoutManager(requireContext()));
        setupAdapter();
    }

    private void setupAdapter() {
        mAdapter = new ImageAdapter(this);
        mRecyclerViewImages.setAdapter(mAdapter);
    }

    private void initView(View view) {
        mSelectImage = view.findViewById(R.id.select_img);
        mRecyclerViewImages = view.findViewById(R.id.recycler_img);
    }

    @Override
    public void onClick(@NonNull View view, int position) {

    }

    @Override
    public void onClick(View v) {
        final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

        final Intent chooser = Intent.createChooser(intent, "Chọn ảnh");
        startActivityForResult(chooser, REQUEST_CODE_SELECT_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGE
                && resultCode == Activity.RESULT_OK
                && data != null) {
            final ClipData clipData = data.getClipData();
            if (clipData != null) {
                final List<Uri> uris = new ArrayList<>();
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    uris.add(clipData.getItemAt(i).getUri());
                }
                mAdapter.setList(uris);
            }
        }
    }
}

class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.VH> {
    private RecyclerOnClickListener mRecyclerOnClickListener;
    private List<Uri> mUris;

    ImageAdapter(RecyclerOnClickListener recyclerOnClickListener) {
        mRecyclerOnClickListener = recyclerOnClickListener;
        mUris = new ArrayList<>();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_iteam_layout, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(mUris.get(position));
    }

    @Override
    public int getItemCount() {
        return mUris.size();
    }

    public void setList(List<Uri> mUris) {
        this.mUris = mUris;
        notifyDataSetChanged();
    }

    class VH extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView mImageViewPreview;
        private ImageView mImageViewClose;

        public VH(@NonNull View itemView) {
            super(itemView);
            mImageViewPreview = itemView.findViewById(R.id.image_preview);
            mImageViewClose = itemView.findViewById(R.id.img_close);

            mImageViewPreview.setOnClickListener(this);
            mImageViewClose.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            if (adapterPosition != NO_POSITION) {
                mRecyclerOnClickListener.onClick(v, adapterPosition);
            }
        }

        public void bind(Uri uri) {
            Picasso.get()
                    .load(uri)
                    .fit()
                    .centerCrop()
                    .into(mImageViewPreview);
        }
    }
}
