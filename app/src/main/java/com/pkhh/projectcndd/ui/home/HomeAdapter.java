package com.pkhh.projectcndd.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.annimon.stream.Stream;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.pkhh.projectcndd.R;

import java.util.List;
import java.util.Objects;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

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
  final String address;
  final long price;

  RoomItem(String id, String title, String address, long price) {
    this.id = id;
    this.title = title;
    this.address = address;
    this.price = price;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RoomItem roomItem = (RoomItem) o;
    return price == roomItem.price &&
        Objects.equals(title, roomItem.title) &&
        Objects.equals(address, roomItem.address);
  }

  @Override
  public int hashCode() {
    return Objects.hash(title, address, price);
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


class HomeAdapter extends ListAdapter<HomeListItem, HomeAdapter.VH> {

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
      return R.layout.saved_room_item_layout;
    }
    if (item instanceof HeaderItem) {
      return android.R.layout.simple_list_item_1;
    }
    throw new IllegalStateException("Unknown view type of item=" + item + ", at position=" + position);
  }

  @NonNull
  @Override
  public VH onCreateViewHolder(@NonNull ViewGroup parent, @LayoutRes int viewType) {
    final View itemView = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
    if (viewType == R.layout.home_banner_item_layout) {
      return new HomeBannerVH(itemView);
    }
    if (viewType == R.layout.saved_room_item_layout) {
      return new RoomItemVH(itemView);
    }
    if (viewType == android.R.layout.simple_list_item_1) {
      return new HeaderVH(itemView);
    }
    throw new IllegalStateException("Unknown viewType=" + viewType);
  }

  @Override
  public void onBindViewHolder(@NonNull VH holder, int position) {
    holder.bind(getItem(position));
  }

  abstract class VH extends RecyclerView.ViewHolder {

    VH(@NonNull View itemView) {
      super(itemView);
    }

    public abstract void bind(HomeListItem item);
  }

  class HomeBannerVH extends VH {
    @BindView(R.id.slider_layout)
    SliderLayout sliderLayout;

    public HomeBannerVH(@NonNull View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }

    @Override
    public void bind(HomeListItem item) {
      if (item instanceof BannerItem) {
        sliderLayout.removeAllSliders();

        Stream.of(((BannerItem) item).images)
            .map((imageAndDescription) -> new TextSliderView(itemView.getContext())
                .description(imageAndDescription.description)
                .image(imageAndDescription.image)
                .setOnSliderClickListener(slider -> {
                  ///TODO
                })
                .setScaleType(BaseSliderView.ScaleType.Fit))
            .forEach(sliderLayout::addSlider);
      } else {
        throw new IllegalStateException("HomeBannerVH::bind only accept parameters type BannerItem");
      }
    }
  }

  class RoomItemVH extends VH {

    @BindView(R.id.text_saved_room_address)
    TextView textAddress;

    @BindView(R.id.text_saved_room_price)
    TextView textPrice;

    @BindView(R.id.text_saved_room_title)
    TextView textTitle;

    RoomItemVH(@NonNull View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }

    @Override
    public void bind(HomeListItem item) {
      if (item instanceof RoomItem) {
        final RoomItem roomItem = (RoomItem) item;
        textAddress.setText(roomItem.address);
        textPrice.setText(roomItem.address);
        textTitle.setText(roomItem.title);
      } else {
        throw new IllegalStateException("RoomItemVH::bind only accept parameters type RoomItem");
      }
    }
  }

  class HeaderVH extends VH{
    @BindView(android.R.id.text1)
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
}