package com.pkhh.projectcndd.screen.saved;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.pkhh.projectcndd.R;
import com.pkhh.projectcndd.models.MotelRoom;
import com.pkhh.projectcndd.utils.RecyclerOnClickListener;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class SavedViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
  public static final DecimalFormat PRICE_FORMAT = new DecimalFormat("###,###");
  private final RecyclerOnClickListener onClickListener;
  @BindView(R.id.image_saved_room) ImageView image;
  @BindView(R.id.image_saved_room_bookmark) AppCompatImageView imageRemoveBookmark;
  @BindView(R.id.text_saved_room_address) TextView textAddress;
  @BindView(R.id.text_saved_room_price) TextView textPrice;
  @BindView(R.id.text_saved_room_title) TextView textTitle;

  public SavedViewHolder(@NonNull View itemView, RecyclerOnClickListener onClickListener) {
    super(itemView);
    this.onClickListener = onClickListener;
    ButterKnife.bind(this, itemView);

    itemView.setOnClickListener(this);
    imageRemoveBookmark.setOnClickListener(this);
  }

  public void bind(MotelRoom item) {
    List<String> imageUrls = item.getImages();
    if (imageUrls == null || imageUrls.isEmpty()) {
      image.setImageResource(R.drawable.ic_home_primary_dark_24dp);
    } else {
      Picasso.get()
          .load(imageUrls.get(0))
          .fit()
          .centerCrop()
          .placeholder(R.drawable.ic_home_primary_dark_24dp)
          .error(R.drawable.ic_home_primary_dark_24dp)
          .into(image);
    }

    textTitle.setText(item.getTitle());
    textAddress.setText(item.getAddress());
    textPrice.setText(
        itemView.getContext()
            .getString(R.string.price_vnd_per_month, PRICE_FORMAT.format(item.getPrice()))
    );
  }

  @Override
  public void onClick(View v) {
    final int adapterPosition = getAdapterPosition();
    if (adapterPosition != RecyclerView.NO_POSITION) {
      onClickListener.onClick(v, adapterPosition);
    }
  }
}
