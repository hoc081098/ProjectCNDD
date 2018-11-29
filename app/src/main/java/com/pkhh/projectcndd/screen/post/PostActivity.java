package com.pkhh.projectcndd.screen.post;

import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Property;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.pkhh.projectcndd.R;
import com.pkhh.projectcndd.models.MotelRoom;
import com.pkhh.projectcndd.service.PostRoomJobIntentService;
import com.pkhh.projectcndd.utils.Constants;
import com.pkhh.projectcndd.utils.DepthPageTransformer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;

import static java.util.Objects.requireNonNull;

public class PostActivity extends AppCompatActivity implements View.OnClickListener {
  public static final int MAX_PAGE = 4;

  private static final TimeInterpolator PROGRESS_ANIM_INTERPOLATOR = new DecelerateInterpolator();
  private static final int ANIM_DURATION = 400;
  private static final Property<ProgressBar, Integer> PROGRESS_PROPERTY = new Property<ProgressBar, Integer>(Integer.class, "progress") {
    @Override
    public Integer get(ProgressBar object) {
      return object.getProgress();
    }

    @Override
    public void set(ProgressBar object, Integer value) {
      object.setProgress(value);
    }
  };

  private DisableScrollViewPager mContainer;
  private ImageView mImagePrev, mImageNext;
  private ProgressBar mProgressBar;
  private ViewGroup mConstraintLayout;
  private View rootLayout;

  @IntRange(from = 0, to = MAX_PAGE - 1)
  private int mCurrentPosition = 0;
  private SelectCategoryFragment mSelectCategoryFragment;
  private SelectAddressLocationFragment mSelectAddressLocationFragment;
  private AddPhotoFragment mAddPhotoFragment;
  private AddPriceTitleSizeDescriptionFragment mAddPriceTitleSizeDescriptionFragment;

  private List<StepFragment> fragmentList;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_post);
    setupActionBar();
    findViews();

    fragmentList = initFragments();
    initStepLayout(fragmentList);

    int pos;
    if (savedInstanceState != null && (pos = savedInstanceState.getInt("CURRENT_POSITION", -1)) >= 0) {
      mCurrentPosition = pos;
      onUpdate(pos);
    }
  }

  private void setupActionBar() {
    final ActionBar supportActionBar = requireNonNull(getSupportActionBar());
    supportActionBar.setDisplayHomeAsUpEnabled(true);
    supportActionBar.setTitle(getString(R.string.post_room));
  }

  private void initStepLayout(List<StepFragment> fragmentList) {
    mContainer.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
      @Override
      public Fragment getItem(int position) {
        return fragmentList.get(position);
      }

      @Override
      public int getCount() {
        return MAX_PAGE;
      }
    });
    mContainer.setOffscreenPageLimit(MAX_PAGE);
    mContainer.setPageTransformer(true, new DepthPageTransformer());

    mProgressBar.setMax(100);
    mProgressBar.setProgress((int) ((double) 1 / MAX_PAGE * 100));

    mImageNext.setOnClickListener(this);
    mImagePrev.setOnClickListener(this);
  }

  @NonNull
  private List<StepFragment> initFragments() {
    mSelectCategoryFragment = new SelectCategoryFragment();
    mSelectAddressLocationFragment = new SelectAddressLocationFragment();
    mAddPhotoFragment = new AddPhotoFragment();
    mAddPriceTitleSizeDescriptionFragment = new AddPriceTitleSizeDescriptionFragment();
    return new ArrayList<>(Arrays.asList(
        mSelectCategoryFragment,
        mSelectAddressLocationFragment,
        mAddPhotoFragment,
        mAddPriceTitleSizeDescriptionFragment
    ));
  }

  private void onUpdate(@IntRange(from = 0, to = MAX_PAGE - 1) int position) {
    changeFragmentByPosition(position);
    setProgressBarProgressWithAnimation((int) ((double) (position + 1) / MAX_PAGE * 100));
    TransitionManager.beginDelayedTransition(mConstraintLayout, new AutoTransition()
        .addTarget(mImageNext)
        .addTarget(mImagePrev)
        .setDuration(ANIM_DURATION));
    mImagePrev.setVisibility(position == 0 ? View.INVISIBLE : View.VISIBLE);
    final Drawable drawable = ContextCompat.getDrawable(this, position == MAX_PAGE - 1 ? R.drawable.ic_done_all_black_24dp : R.drawable.ic_navigate_next_black_24dp);
    mImageNext.setImageDrawable(drawable);
  }

  private void setProgressBarProgressWithAnimation(int progress) {
    final ObjectAnimator animator = ObjectAnimator.ofInt(mProgressBar, PROGRESS_PROPERTY, mProgressBar.getProgress(), progress);
    animator.setDuration(ANIM_DURATION);
    animator.setInterpolator(PROGRESS_ANIM_INTERPOLATOR);
    animator.start();
  }

  private void changeFragmentByPosition(@IntRange(from = 0, to = MAX_PAGE - 1) int position) {
    final ActionBar supportActionBar = requireNonNull(getSupportActionBar());

    if (position == 0) {
      supportActionBar.setTitle(getString(R.string.select_category));
    } else if (position == 1) {
      supportActionBar.setTitle(getString(R.string.add_address));
    } else if (position == 2) {
      supportActionBar.setTitle(getString(R.string.add_image));
    } else if (position == 3) {
      supportActionBar.setTitle(getString(R.string.add_info));
    }
    mContainer.setCurrentItem(position, true);
  }

  private void findViews() {
    mContainer = findViewById(R.id.post_container);
    mImageNext = findViewById(R.id.button_next);
    mImagePrev = findViewById(R.id.button_prev);
    mProgressBar = findViewById(R.id.progressBar2);
    mConstraintLayout = findViewById(R.id.constraintLayout);
    rootLayout = findViewById(R.id.root_post_activity);
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.button_next:

        if (fragmentList.get(mCurrentPosition).canGoNext()) {
          goNext();
        }

        break;
      case R.id.button_prev:
        goPrevious();
        break;
    }

  }

  // return true, nếu có thể quay về fragment trước, false ngược lại
  private boolean goPrevious() {
    if (mCurrentPosition > 0) {
      --mCurrentPosition;
      onUpdate(mCurrentPosition);
      return true;
    }
    return false;
  }

  private void goNext() {
    if (mCurrentPosition == MAX_PAGE - 1) {
      onComplete();
    } else {
      ++mCurrentPosition;
      onUpdate(mCurrentPosition);
      if (fragmentList.get(mCurrentPosition) instanceof AddPriceTitleSizeDescriptionFragment) {
        mAddPriceTitleSizeDescriptionFragment.setDistrictName(mSelectAddressLocationFragment.getDataOutput().getDistrictName());
      }
    }
  }

  @Override
  public void onBackPressed() {
    if (!goPrevious()) {
      super.onBackPressed();
    }
  }


  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      onBackPressed();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void onComplete() {
    final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    final String uid = requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

    final Intent intent = new Intent(this, PostRoomJobIntentService.class);
    intent.putExtra("room", getRoom(firestore, uid));
    intent.putExtra("uris", new ArrayList<>(mAddPhotoFragment.getDataOutput().getUris()));
    PostRoomJobIntentService.enqueue(this, intent);

    finish();
  }

  private MotelRoom getRoom(FirebaseFirestore firestore, String uid) {
    MotelRoom room = new MotelRoom();

    // category
    room.setCategory(firestore.document(Constants.CATEGORIES_NAME_COLLECION + "/" + requireNonNull(mSelectCategoryFragment.getDataOutput()).getSelectedCategoryId()));

    // address
    room.setAddress(mSelectAddressLocationFragment.getDataOutput().getAddress());
    room.setAddressGeoPoint(new GeoPoint(requireNonNull(mSelectAddressLocationFragment.getDataOutput().getLatLng()).latitude, mSelectAddressLocationFragment.getDataOutput().getLatLng().longitude));
    final DocumentReference provinceRef = firestore.document(Constants.PROVINCES_NAME_COLLECION + "/" + mSelectAddressLocationFragment.getDataOutput().getProvinceId());
    room.setProvince(provinceRef);
    final DocumentReference districtRef = provinceRef.collection(Constants.DISTRICTS_NAME_COLLECION).document(requireNonNull(mSelectAddressLocationFragment.getDataOutput().getDistrictId()));
    room.setDistrict(districtRef);
    final DocumentReference wardRef = districtRef.collection(Constants.WARDS_NAME_COLLECION).document(requireNonNull(mSelectAddressLocationFragment.getDataOutput().getWardId()));
    room.setWard(wardRef);

    // some others
    room.setApprove(false);
    room.setCountView(0);
    room.setUpdatedAt(null);
    room.setActive(true);


    room.setSize(mAddPriceTitleSizeDescriptionFragment.getDataOutput().getSize());
    room.setTitle(mAddPriceTitleSizeDescriptionFragment.getDataOutput().getTitle());
    room.setDescription(mAddPriceTitleSizeDescriptionFragment.getDataOutput().getDescription());
    room.setPrice(mAddPriceTitleSizeDescriptionFragment.getDataOutput().getPrice());
    room.setPhone(mAddPriceTitleSizeDescriptionFragment.getDataOutput().getPhone());

    room.setUtilities(new HashMap<>());
    room.setUserIdsSaved(Collections.emptyList());
    room.setUser(firestore.document(Constants.USERS_NAME_COLLECION + "/" + uid));
    room.setImages(Collections.emptyList());

    room.setDistrictName(mSelectAddressLocationFragment.getDataOutput().getDistrictName());
    return room;
  }
}
