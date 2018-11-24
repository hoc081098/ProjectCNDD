package com.pkhh.projectcndd.screen.home;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.daimajia.slider.library.Transformers.DefaultTransformer;
import com.daimajia.slider.library.Transformers.DepthPageTransformer;
import com.daimajia.slider.library.Transformers.FadeTransformer;
import com.daimajia.slider.library.Transformers.FlipHorizontalTransformer;
import com.daimajia.slider.library.Transformers.FlipPageViewTransformer;
import com.daimajia.slider.library.Transformers.RotateDownTransformer;
import com.daimajia.slider.library.Transformers.RotateUpTransformer;
import com.daimajia.slider.library.Transformers.StackTransformer;
import com.daimajia.slider.library.Transformers.TabletTransformer;
import com.daimajia.slider.library.Transformers.ZoomInTransformer;
import com.daimajia.slider.library.Transformers.ZoomOutSlideTransformer;
import com.daimajia.slider.library.Transformers.ZoomOutTransformer;
import com.google.android.material.textfield.TextInputLayout;
import com.pkhh.projectcndd.R;
import com.pkhh.projectcndd.screen.detail.MotelRoomDetailActivity;
import com.pkhh.projectcndd.screen.search.SearchActivity;
import com.squareup.picasso.Picasso;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import androidx.annotation.IntDef;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;
import static com.pkhh.projectcndd.utils.Constants.MOTEL_ROOM_ID;

interface HomeListItem {
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
  final long price;
  final String address;
  final String districtName;
  @Nullable final String image;
  @BookMarkIconState final int bookMarkIconState;

  RoomItem(String id, String title, long price, String address, String districtName, @Nullable String image, int bookMarkIconState) {
    this.id = id;
    this.title = title;
    this.price = price;
    this.address = address;
    this.districtName = districtName;
    this.image = image;
    this.bookMarkIconState = bookMarkIconState;
  }

  @IntDef(value = {HIDE, SHOW_NOT_SAVED, SHOW_SAVED})
  @Retention(RetentionPolicy.SOURCE)
  @interface BookMarkIconState {}

  public static final int HIDE = 2;
  public static final int SHOW_NOT_SAVED = 3;
  public static final int SHOW_SAVED = 4;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RoomItem roomItem = (RoomItem) o;
    return bookMarkIconState == roomItem.bookMarkIconState &&
        Objects.equals(id, roomItem.id) &&
        Objects.equals(title, roomItem.title) &&
        Objects.equals(price, roomItem.price) &&
        Objects.equals(address, roomItem.address) &&
        Objects.equals(districtName, roomItem.districtName) &&
        Objects.equals(image, roomItem.image);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, title, price, address, districtName, image, bookMarkIconState);
  }
}

class BannerItem implements HomeListItem {
  final List<ImageAndDescriptionBanner> images;
  final CharSequence selectedProvinceName;

  BannerItem(List<ImageAndDescriptionBanner> images, CharSequence selectedProvinceName) {
    this.images = images;
    this.selectedProvinceName = selectedProvinceName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BannerItem that = (BannerItem) o;
    return Objects.equals(images, that.images) && Objects.equals(selectedProvinceName, that.selectedProvinceName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(images, selectedProvinceName);
  }
}

class SeeAll implements HomeListItem {
  public static final int COUNT_VIEW_DESCENDING = 2;
  public static final int CREATED_AT_DESCENDING = 3;

  @IntDef(value = {CREATED_AT_DESCENDING, COUNT_VIEW_DESCENDING})
  @interface QueryDirection {}

  @QueryDirection final int queryDirection;

  SeeAll(@QueryDirection int queryDirection) {
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
  private static final DecimalFormat PRICE_FORMAT = new DecimalFormat("###,###");
  static final String QUERY_DIRECTION = "QUERY_DIRECTION";

  @NonNull
  private final Function1<String, Void> onAddToOrRemoveFromSavedRooms;

  @NonNull
  private final Function0<Void> onChangeLocationClick;

  HomeAdapter(@NonNull Function1<String, Void> onAddToOrRemoveFromSavedRooms, @NonNull Function0<Void> onChangeLocationClick) {
    super(new DiffUtil.ItemCallback<HomeListItem>() {
      @Override
      public boolean areItemsTheSame(@NonNull HomeListItem oldItem, @NonNull HomeListItem newItem) {
        if (oldItem instanceof RoomItem && newItem instanceof RoomItem) {
          return Objects.equals(((RoomItem) oldItem).id, ((RoomItem) newItem).id);
        }
        if (oldItem instanceof HeaderItem && newItem instanceof HeaderItem) {
          return Objects.equals(((HeaderItem) oldItem).title, ((HeaderItem) newItem).title);
        }
        if (oldItem instanceof BannerItem && newItem instanceof BannerItem) {
          return oldItem.equals(newItem);
        }
        if (oldItem instanceof SeeAll && newItem instanceof SeeAll) {
          return Objects.equals(((SeeAll) oldItem).queryDirection, ((SeeAll) newItem).queryDirection);
        }
        return Objects.equals(oldItem, newItem);
      }

      @Override
      public boolean areContentsTheSame(@NonNull HomeListItem oldItem, @NonNull HomeListItem newItem) {
        return oldItem.equals(newItem);
      }
    });
    this.onAddToOrRemoveFromSavedRooms = onAddToOrRemoveFromSavedRooms;
    this.onChangeLocationClick = onChangeLocationClick;
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

  class HomeBannerVH extends VH {
    private final BaseTransformer[] TRANSFORMERS = new BaseTransformer[]{
        new AccordionTransformer(),
        new BackgroundToForegroundTransformer(),
        new CubeInTransformer(),
        new FlipHorizontalTransformer(),
        new FlipPageViewTransformer(),
        new FadeTransformer(),
        new DepthPageTransformer(),
        new DefaultTransformer(),
        new CubeInTransformer(),
        new RotateDownTransformer(),
        new RotateUpTransformer(),
        new StackTransformer(),
        new TabletTransformer(),
        new ZoomInTransformer(),
        new ZoomOutSlideTransformer(),
        new ZoomOutTransformer()
    };
    private final Random random = new Random();

    @BindView(R.id.slider_layout) SliderLayout sliderLayout;
    @BindView(R.id.button_change_loc) Button buttonChangeLocation;
    @BindView(R.id.edit_text_search) TextInputLayout textInputLayout;

    public HomeBannerVH(@NonNull View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
      textInputLayout.getEditText().setKeyListener(null);
    }

    @OnClick({R.id.button_change_loc, R.id.edit_text_search})
    public void onClick(View v) {
      if (v.getId() == R.id.edit_text_search) {

        final Context context = v.getContext();
        context.startActivity(new Intent(context, SearchActivity.class));

      } else if (v.getId() == R.id.button_change_loc) {

        onChangeLocationClick.invoke();

      }
    }

    @Override
    public void bind(HomeListItem item) {
      if (!(item instanceof BannerItem)) {
        throw new IllegalStateException("HomeBannerVH::bind only accept parameters type BannerItem");
      }
      Log.d("@@@", "HomeBannerVH::bind");
      final BannerItem bannerItem = (BannerItem) item;

      buttonChangeLocation.setText(bannerItem.selectedProvinceName);

      sliderLayout.removeAllSliders();
      Stream.of(bannerItem.images)
          .map((imageAndDescription) -> new TextSliderView(itemView.getContext())
              .description(imageAndDescription.description)
              .image(imageAndDescription.image)
              .setOnSliderClickListener(slider -> {
                ///TODO
              })
              .setScaleType(BaseSliderView.ScaleType.Fit))
          .forEach(sliderLayout::addSlider);

      sliderLayout.setPagerTransformer(true, TRANSFORMERS[random.nextInt(TRANSFORMERS.length)]);
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

    @BindView(R.id.image_saved_room_bookmark)
    ImageView imageSave;

    RoomItemVH(@NonNull View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);

      itemView.setOnClickListener(this);
      imageSave.setOnClickListener(this);
    }

    @Override
    public void bind(HomeListItem item) {
      if (!(item instanceof RoomItem)) {
        throw new IllegalStateException("RoomItemVH::bind only accept parameters type RoomItem");
      }

      final RoomItem room = (RoomItem) item;

      textDistrict.setText(room.districtName);
      textAddress.setText(room.address);
      textPrice.setText(PRICE_FORMAT.format(room.price) + "đ/tháng");
      textTitle.setText(room.title);

      Picasso.get()
          .load(room.image)
          .fit()
          .centerCrop()
          .placeholder(R.drawable.ic_home_primary_dark_24dp)
          .error(R.drawable.ic_home_primary_dark_24dp)
          .into(image);

      switch (room.bookMarkIconState) {
        case RoomItem.HIDE:
          imageSave.setVisibility(View.INVISIBLE);
          break;
        case RoomItem.SHOW_SAVED:
          imageSave.setVisibility(View.VISIBLE);
          imageSave.setImageResource(R.drawable.ic_bookmark_white_24dp);
          break;
        case RoomItem.SHOW_NOT_SAVED:
          imageSave.setVisibility(View.VISIBLE);
          imageSave.setImageResource(R.drawable.ic_bookmark_border_white_24dp);
          break;
      }

    }

    @Override
    public void onClick(View v) {
      final int adapterPosition = getAdapterPosition();
      if (adapterPosition != NO_POSITION) {
        final HomeListItem item = getItem(adapterPosition);
        if (item instanceof RoomItem) {
          final String id = ((RoomItem) item).id;

          if (v.getId() == R.id.image_saved_room_bookmark) {
            onAddToOrRemoveFromSavedRooms.invoke(id);
          } else {

            final Context context = itemView.getContext();
            final Intent intent = new Intent(context, MotelRoomDetailActivity.class);
            intent.putExtra(MOTEL_ROOM_ID, id);
            context.startActivity(intent);

          }
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
          final int queryDirection = ((SeeAll) item).queryDirection;
          final Context context = itemView.getContext();
          final Intent intent = new Intent(context, ShowMoreActivity.class);
          intent.putExtra(QUERY_DIRECTION, queryDirection);
          context.startActivity(intent);
        }
      }
    }
  }
}