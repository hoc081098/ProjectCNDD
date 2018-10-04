package com.pkhh.projectcndd.ui.home;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.pkhh.projectcndd.R;
import com.pkhh.projectcndd.utils.RecyclerOnClickListener;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

public class MotelRoomVH extends RecyclerView.ViewHolder implements View.OnClickListener {
    public static final DecimalFormat decimalFormat = new DecimalFormat("###,###");

    private final ImageView imagePreview;
    private final TextView textPrice;
    private final TextView textAddress;
    private final TextView textPostBy;
    private final ImageView imageShare;
    private final ImageView imageSave;
    private final RecyclerOnClickListener recyclerClickListener;
    private final FirebaseFirestore firestore;

    MotelRoomVH(@NonNull View itemView, @NonNull RecyclerOnClickListener recyclerClickListener) {
        super(itemView);
        textPrice = itemView.findViewById(R.id.text_price);
        textAddress = itemView.findViewById(R.id.text_address);
        textPostBy = itemView.findViewById(R.id.text_post_by);
        imageShare = itemView.findViewById(R.id.image_share);
        imageSave = itemView.findViewById(R.id.image_save);
        imagePreview = itemView.findViewById(R.id.image_preview);

        this.recyclerClickListener = recyclerClickListener;

        imageSave.setOnClickListener(this);
        imageShare.setOnClickListener(this);
        itemView.setOnClickListener(this);


        firestore = FirebaseFirestore.getInstance();
    }

    void bind(RoomItem item) {
        textPrice.setText("$ " + decimalFormat.format(item.motelRoom.getPrice()) + " đ");
        textAddress.setText(item.motelRoom.getAddress());

        item.motelRoom.getUser()
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    textPostBy.setText("đăng bởi " + documentSnapshot.getString("full_name"));
                })
                .addOnFailureListener(e -> {
                    textPostBy.setText("đăng bởi ...");
                });

        List<String> imageUrls = item.motelRoom.getImages();
        if (imageUrls != null && !imageUrls.isEmpty()) {
            Picasso.get()
                    .load(imageUrls.get(0))
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.ic_home_black_24dp)
                    .error(R.drawable.ic_home_black_24dp)
                    .into(imagePreview);
        }

        if (item.isLogined) {
            imageSave.setVisibility(View.VISIBLE);

            if (item.isSaved) {
                imageSave.setImageResource(R.drawable.ic_bookmark_white_24dp);
            } else {
                imageSave.setImageResource(R.drawable.ic_bookmark_border_white_24dp);
            }
        } else {
            imageSave.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public void onClick(View v) {
        final int adapterPosition = getAdapterPosition();
        if (adapterPosition != NO_POSITION) {
            recyclerClickListener.onClick(v, adapterPosition);
        }
    }
}