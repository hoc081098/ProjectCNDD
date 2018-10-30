package com.pkhh.projectcndd.ui.post;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.pkhh.projectcndd.R;
import com.pkhh.projectcndd.utils.RecyclerOnClickListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.VH> {
    private RecyclerOnClickListener mRecyclerOnClickListener;
    private List<Uri> mUris;

    ImageAdapter(RecyclerOnClickListener recyclerOnClickListener, List<Uri> uris) {
        mRecyclerOnClickListener = recyclerOnClickListener;
        mUris = uris;
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

    class VH extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView mImageViewPreview;
        private ImageView mImageViewClose;

        public VH(@NonNull View itemView) {
            super(itemView);
            mImageViewPreview = itemView.findViewById(R.id.img_preview);
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
            Picasso.get()
                    .load(R.drawable.ic_action_close)
                    .fit()
                    .centerCrop()
                    .noFade()
                    .into(mImageViewClose);
        }
    }
}