package com.pkhh.projectcndd.ui.home;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.annimon.stream.Stream;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.daimajia.slider.library.Transformers.AccordionTransformer;
import com.daimajia.slider.library.Transformers.BackgroundToForegroundTransformer;
import com.daimajia.slider.library.Transformers.BaseTransformer;
import com.daimajia.slider.library.Transformers.CubeInTransformer;
import com.daimajia.slider.library.Transformers.FlipHorizontalTransformer;
import com.daimajia.slider.library.Transformers.RotateDownTransformer;
import com.google.firebase.firestore.DocumentReference;
import com.pkhh.projectcndd.R;
import com.pkhh.projectcndd.ui.detail.MotelRoomDetailActivity;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;
import static com.pkhh.projectcndd.utils.Constants.MOTEL_ROOM_ID;

interface HomeListItem {
}

enum QueryDirection {
  VIEW_COUNT_DESCENDING,
  CREATED_AT_DESCENDING
}

class ImageAndDescriptionBanner {
  final String image;
  final String description;

  ImageAndDescriptionBanner(String image, String description) {
    this.image = image;
    this.description = description;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ImageAndDescriptionBanner that = (ImageAndDescriptionBanner) o;
    return Objects.equals(image, that.image) &&
        Objects.equals(description, that.description);
  }

  @Override
  public int hashCode() {
    return Objects.hash(image, description);
  }
}

class HeaderItem implements HomeListItem {
  final String title;

  HeaderItem(String title) {
    this.title = title;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    HeaderItem that = (HeaderItem) o;
    return Objects.equals(title, that.title);
  }

  @Override
  public int hashCode() {
    return Objects.hash(title);
  }
}

class RoomItem implements HomeListItem {
  final String id;
  final String title;
  final String address;
  final long price;
  @Nullable
  final List<String> images;
  final DocumentReference district;

  RoomItem(String id, String title, String address, long price, @Nullable List<String> images, DocumentReference district) {
    this.id = id;
    this.title = title;
    this.address = address;
    this.price = price;
    this.images = images;
    this.district = district;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RoomItem roomItem = (RoomItem) o;
    return price == roomItem.price &&
        Objects.equals(id, roomItem.id) &&
        Objects.equals(title, roomItem.title) &&
        Objects.equals(address, roomItem.address) &&
        Objects.equals(images, roomItem.images) &&
        Objects.equals(district, roomItem.district);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, title, address, price, images, district);
  }
}

class BannerItem implements HomeListItem {
  final List<ImageAndDescriptionBanner> images;

  BannerItem(List<ImageAndDescriptionBanner> images) {
    this.images = images;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BannerItem that = (BannerItem) o;
    return Objects.equals(images, that.images);
  }

  @Override
  public int hashCode() {
    return Objects.hash(images);
  }
}

class SeeAll implements HomeListItem {
  final QueryDirection queryDirection;

  SeeAll(QueryDirection queryDirection) {
    this.queryDirection = queryDirection;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SeeAll seeAll = (SeeAll) o;
    return queryDirection == seeAll.queryDirection;
  }

  @Override
  public int hashCode() {
    return Objects.hash(queryDirection);
  }
}

class HomeAdapter extends ListAdapter<HomeListItem, HomeAdapter.VH> {
  public static final DecimalFormat PRICE_FORMAT = new DecimalFormat("###,###");
  public static final String QUERY_DIRECTION = "QUERY_DIRECTION";

  protected HomeAdapter() {
    super(new DiffUtil.ItemCallback<HomeListItem>() {
      @Override
      public boolean areItemsTheSame(@NonNull HomeListItem oldItem, @NonNull HomeListItem newItem) {
        if (oldItem instanceof RoomItem && newItem instanceof RoomItem) {
          return ((RoomItem) oldItem).id.equals(((RoomItem) newItem).id);
        }
        return oldItem.equals(newItem);
      }

      @Override
      public boolean areContentsTheSame(@NonNull HomeListItem oldItem, @NonNull HomeListItem newItem) {
        return oldItem.equals(newItem);
      }
    });
  }

  @Override
  @LayoutRes
  public int getItemViewType(int position) {
    final HomeListItem item = getItem(position);
    if (item instanceof BannerItem) {
      return R.layout.home_banner_item_layout;
    }
    if (item instanceof RoomItem) {
      return R.layout.home_room_item_layout;
    }
    if (item instanceof HeaderItem) {
      return R.layout.home_header_item_layout;
    }
    if (item instanceof SeeAll) {
      return R.layout.home_seeall_item_layout;
    }
    throw new IllegalStateException("Unknown view type of item=" + item + ", at position=" + position);
  }

  @NonNull
  @Override
  public VH onCreateViewHolder(@NonNull ViewGroup parent, @LayoutRes int viewType) {
    final View itemView = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
    switch (viewType) {
      case R.layout.home_banner_item_layout:
        return new HomeBannerVH(itemView);
      case R.layout.home_room_item_layout:
        return new RoomItemVH(itemView);
      case R.layout.home_header_item_layout:
        return new HeaderVH(itemView);
      case R.layout.home_seeall_item_layout:
        return new SeaAllVH(itemView);
      default:
        throw new IllegalStateException("Unknown viewType=" + viewType);
    }
  }

  @Override
  public void onBindViewHolder(@NonNull VH holder, int position) {
    holder.bind(getItem(position));
  }

  public static abstract class VH extends RecyclerView.ViewHolder {

    VH(@NonNull View itemView) {
      super(itemView);
    }

    public abstract void bind(HomeListItem item);
  }

  static class HomeBannerVH extends VH {
    private static final BaseTransformer[] TRANSFORMERS = new BaseTransformer[]{
        new AccordionTransformer(),
        new BackgroundToForegroundTransformer(),
        new CubeInTransformer(),
        new FlipHorizontalTransformer(),
        new RotateDownTransformer()
    };

    @BindView(R.id.slider_layout)
    SliderLayout sliderLayout;

    public HomeBannerVH(@NonNull View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }

    @Override
    public void bind(HomeListItem item) {
      if (!(item instanceof BannerItem)) {
        throw new IllegalStateException("HomeBannerVH::bind only accept parameters type BannerItem");
      }
      Log.d("@@@", "HomeBannerVH::bind");
      sliderLayout.removeAllSliders();
      sliderLayout.setPagerTransformer(true, TRANSFORMERS[new Random().nextInt(TRANSFORMERS.length)]);
      Stream.of(((BannerItem) item).images)
          .map((imageAndDescription) -> new TextSliderView(itemView.getContext())
              .description(imageAndDescription.description)
              .image(imageAndDescription.image)
              .setOnSliderClickListener(slider -> {
                ///TODO
              })
              .setScaleType(BaseSliderView.ScaleType.Fit))
          .forEach(sliderLayout::addSlider);
    }
  }

  class RoomItemVH extends VH implements View.OnClickListener {

    @BindView(R.id.text_saved_room_address)
    TextView textAddress;

    @BindView(R.id.text_saved_room_price)
    TextView textPrice;

    @BindView(R.id.text_saved_room_title)
    TextView textTitle;

    @BindView(R.id.image_saved_room)
    ImageView image;

    @BindView(R.id.text_home_room_district)
    TextView textDistrict;

    RoomItemVH(@NonNull View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
      itemView.setOnClickListener(this);
    }

    @Override
    public void bind(HomeListItem item) {
      if (item instanceof RoomItem) {
        final RoomItem roomItem = (RoomItem) item;

        textDistrict.setText("loading...");
        roomItem.district.get().addOnSuccessListener(documentSnapshot -> {
          textDistrict.setText(documentSnapshot.getString("name"));
        }).addOnFailureListener(e -> {
          textDistrict.setText("error...");
        });

        textAddress.setText(roomItem.address);
        textPrice.setText(PRICE_FORMAT.format(roomItem.price) + "đ/tháng");
        textTitle.setText(roomItem.title);

        if (roomItem.images == null || roomItem.images.isEmpty()) {
          image.setImageResource(R.drawable.ic_home_primary_dark_24dp);
        } else {
          Picasso.get()
              .load(roomItem.images.get(0))
              .fit()
              .centerCrop()
              .placeholder(R.drawable.ic_home_primary_dark_24dp)
              .error(R.drawable.ic_home_primary_dark_24dp)
              .into(image);
        }

      } else {
        throw new IllegalStateException("RoomItemVH::bind only accept parameters type RoomItem");
      }
    }

    @Override
    public void onClick(View v) {
      final int adapterPosition = getAdapterPosition();
      if (adapterPosition != NO_POSITION) {
        final HomeListItem item = getItem(adapterPosition);
        if (item instanceof RoomItem) {

          final String id = ((RoomItem) item).id;
          final Context context = itemView.getContext();
          final Intent intent = new Intent(context, MotelRoomDetailActivity.class);
          intent.putExtra(MOTEL_ROOM_ID, id);
          context.startActivity(intent);

        }
      }
    }
  }

  static class HeaderVH extends VH {
    @BindView(R.id.text_header_title)
    TextView textView;

    HeaderVH(@NonNull View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }

    @Override
    public void bind(HomeListItem item) {
      if (item instanceof HeaderItem) {
        textView.setText(((HeaderItem) item).title);
      } else {
        throw new IllegalStateException("HeaderVH::bind only accept parameters type HeaderItem");
      }
    }
  }

  class SeaAllVH extends VH implements View.OnClickListener {
    SeaAllVH(@NonNull View itemView) {
      super(itemView);
      itemView.findViewById(R.id.button_see_all).setOnClickListener(this);
    }

    @Override
    public void bind(HomeListItem item) {
    }

    @Override
    public void onClick(View v) {
      final int adapterPosition = getAdapterPosition();
      if (adapterPosition != NO_POSITION) {
        final HomeListItem item = getItem(adapterPosition);
        if (item instanceof SeeAll) {
          final QueryDirection queryDirection = ((SeeAll) item).queryDirection;
          final Context context = itemView.getContext();
          final Intent intent = new Intent(context, TestActivity.class);
          intent.putExtra(QUERY_DIRECTION, queryDirection);
          context.startActivity(intent);
        }
      }
    }
  }
}