package com.pkhh.projectcndd.screen.detail;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.github.chrisbanes.photoview.PhotoView;
import com.pkhh.projectcndd.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import butterknife.ButterKnife;

import static com.pkhh.projectcndd.utils.Constants.EXTRA_IMAGES;
import static com.pkhh.projectcndd.utils.Constants.EXTRA_INDEX;

public class PhotoSlideActivity extends AppCompatActivity {

  @BindView(R.id.view_pager) ViewPager viewPager;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_photo_slide);
    ButterKnife.bind(this, this);

    final Intent intent = getIntent();
    @Nullable final ArrayList<String> imagesString = intent.getStringArrayListExtra(EXTRA_IMAGES);
    @SuppressWarnings("unchecked") final ArrayList<Uri> imagesUri = (ArrayList<Uri>) intent.getSerializableExtra(EXTRA_IMAGES);
    final int index = intent.getIntExtra(EXTRA_INDEX, 0);

    viewPager.setAdapter(new PagerAdapter() {
      @Override
      public int getCount() { return imagesString != null ? imagesString.size() : imagesUri.size(); }

      @Override
      public boolean isViewFromObject(@NonNull View view, @NonNull Object object) { return view == object; }

      @Override
      public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
      }

      @NonNull
      @Override
      public Object instantiateItem(@NonNull ViewGroup container, int position) {
        final PhotoView photoView = new PhotoView(container.getContext());

        // load image
        if (imagesString != null) {
          Picasso.get()
              .load(imagesString.get(position))
              .fit()
              .centerCrop()
              .into(photoView);
        } else {
          Picasso.get()
              .load(imagesUri.get(position))
              .fit()
              .centerCrop()
              .into(photoView);
        }

        // Now just add PhotoView to ViewPager and return it
        container.addView(photoView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        return photoView;
      }
    });
    viewPager.setCurrentItem(index, true);
  }
}
