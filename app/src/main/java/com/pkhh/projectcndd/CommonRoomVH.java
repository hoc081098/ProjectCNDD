package com.pkhh.projectcndd;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.pkhh.projectcndd.models.MotelRoom;
import com.pkhh.projectcndd.utils.RecyclerOnClickListener;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class CommonRoomVH extends RecyclerView.ViewHolder {
  public static final DecimalFormat PRICE_FORMAT = new DecimalFormat("###,###");

  @BindView(R.id.image_room) ImageView imageView;
  @BindView(R.id.text_room_title) TextView textTitle;
  @BindView(R.id.text_room_price) TextView textPrice;
  @BindView(R.id.text_room_address) TextView textAddress;

  public CommonRoomVH(@NonNull View itemView, final RecyclerOnClickListener onItemClick) {
    super(itemView);
    ButterKnife.bind(this, itemView);
    itemView.setOnClickListener(v -> {
      final int position = getAdapterPosition();
      if (position != RecyclerView.NO_POSITION) {
        onItemClick.onClick(v, position);
      }
    });
  }

  public void bind(MotelRoom item) {
    List<String> imageUrls = item.getImages();
    if (imageUrls == null || imageUrls.isEmpty()) {
      imageView.setImageResource(R.drawable.ic_home_primary_dark_24dp);
    } else {
      Picasso.get()
          .load(imageUrls.get(0))
          .fit()
          .centerCrop()
          .placeholder(R.drawable.ic_home_primary_dark_24dp)
          .error(R.drawable.ic_home_primary_dark_24dp)
          .into(imageView);
    }

    textTitle.setText(item.getTitle());
    textAddress.setText(item.getAddress());
    textPrice.setText(
        itemView.getContext()
            .getString(R.string.price_vnd_per_month, PRICE_FORMAT.format(item.getPrice()))
    );
  }
}