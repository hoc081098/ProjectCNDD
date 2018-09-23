package com.pkhh.projectcndd.ui.post;

import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Property;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.pkhh.projectcndd.R;

import java.util.Arrays;
import java.util.List;

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
    public static final int MAX_PAGE = 3;

    private static final TimeInterpolator PROGRESS_ANIM_INTERPOLATOR = new DecelerateInterpolator();
    private static final int ANIM_DURATION = 300;
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

    private int mCurrentPosition = 0;
    private SelectCategoryFragment mSelectCategoryFragment;
    private SelectLocationFragment mSelectLocationFragment;
    private AddPhotoFragment mAddPhotoFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        setupActionBar();
        findViews();
        final List<Fragment> fragmentList = initFragments();
        initStepLayout(fragmentList);
    }

    private void setupActionBar() {
        final ActionBar supportActionBar = requireNonNull(getSupportActionBar());
        supportActionBar.setDisplayHomeAsUpEnabled(true);
        supportActionBar.setTitle("Bạn đăng tin");
    }

    private void initStepLayout(List<Fragment> fragmentList) {
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

        mProgressBar.setMax(100);
        mProgressBar.setProgress((int) ((double) 1 / MAX_PAGE * 100));

        mImageNext.setOnClickListener(this);
        mImagePrev.setOnClickListener(this);
    }

    @NonNull
    private List<Fragment> initFragments() {
        mSelectCategoryFragment = new SelectCategoryFragment();
        mSelectLocationFragment = new SelectLocationFragment();
        mAddPhotoFragment = new AddPhotoFragment();
        return Arrays.asList(
                mSelectCategoryFragment,
                mSelectLocationFragment,
                mAddPhotoFragment
        );
    }

    private void onUpdate(int position) {
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

    private void changeFragmentByPosition(int position) {
        final ActionBar supportActionBar = requireNonNull(getSupportActionBar());

        if (position == 0) {
            supportActionBar.setTitle("Chọn thể loại");
        } else if (position == 1) {
            supportActionBar.setTitle("Thêm địa chỉ");
        } else if (position == 2) {
            supportActionBar.setTitle("Thêm ảnh");
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
                final String message = getError();

                // không thể đi tiếp
                if (message != null) {
                    Snackbar.make(rootLayout, message, Snackbar.LENGTH_SHORT)
                            .show();
                    return;
                }

                // đi tiếp
                if (mCurrentPosition == MAX_PAGE - 1) {
                    onComplete();
                } else {
                    ++mCurrentPosition;
                    onUpdate(mCurrentPosition);
                }
                break;
            case R.id.button_prev:
                if (mCurrentPosition == 0) {
                    onUpdate(mCurrentPosition);
                } else {
                    --mCurrentPosition;
                    onUpdate(mCurrentPosition);
                }
                break;
        }

    }

    // return null nếu có thể đi tiếp, hoặc return string thông báo lỗi
    @Nullable
    private String getError() {
        if (mCurrentPosition == 0) {
            if (mSelectCategoryFragment.getSelectedCategory() != null) {
                return null;
            } else {
                return "Hãy chọn một thể loại!";
            }
        }
        if (mCurrentPosition == 1) {
            final String districtId = mSelectLocationFragment.getDistrictId();
            final String provinceId = mSelectLocationFragment.getProvinceId();
            final String wardId = mSelectLocationFragment.getWardId();
            if (districtId != null && provinceId != null && wardId != null) {
                return null;
            } else {
                return "Hãy cung cấp đủ địa chỉ!";
            }
        }

        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.post, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        if (item.getItemId() == R.id.action_review) {
            //TODO: Xem lại thông tin
            Toast.makeText(this, "TODO xem lại thông tin", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void onComplete() {
        Toast.makeText(this, "onComplete", Toast.LENGTH_SHORT).show();
        // TODO: Đăng bài
    }
}
