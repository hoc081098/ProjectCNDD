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

import com.pkhh.projectcndd.R;

import java.util.Arrays;
import java.util.List;

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
    public static final String CURRENT_POSITION = "CURRENT_POSITION";
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
    private int mCurrentPosition = 0;

    private FragmentSelectCategory mFragmentSelectCategory;
    private Fragment2 mFragment2;
    private Fragment3 mFragment3;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        final ActionBar supportActionBar = requireNonNull(getSupportActionBar());
        supportActionBar.setDisplayHomeAsUpEnabled(true);
        supportActionBar.setTitle("Bạn đăng tin");

        initViews();

        mFragmentSelectCategory = new FragmentSelectCategory();
        mFragment2 = new Fragment2();
        mFragment3 = new Fragment3();
        final List<Fragment> fragmentList = Arrays.asList(mFragmentSelectCategory, mFragment2, mFragment3);

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

    private void onUpdate(int position) {
        changeFragmentByPosiyion(position);
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

    private void changeFragmentByPosiyion(int position) {
        final ActionBar supportActionBar = requireNonNull(getSupportActionBar());

        if (position == 0) {
        } else if (position == 1) {
            mFragment2.updateText(mFragmentSelectCategory.getSelectedCategory() != null ? mFragmentSelectCategory.getSelectedCategory().name : null);
        } else if (position == 2) {
        }

        mContainer.setCurrentItem(position, true);
    }

    private void initViews() {
        mContainer = findViewById(R.id.post_container);
        mImageNext = findViewById(R.id.button_next);
        mImagePrev = findViewById(R.id.button_prev);
        mProgressBar = findViewById(R.id.progressBar2);
        mConstraintLayout = findViewById(R.id.constraintLayout);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_next:
                if (!canGoNext()) {
                    Toast.makeText(this, getError(), Toast.LENGTH_SHORT).show();
                    return;
                }

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


    private String getError() {
        if (mCurrentPosition == 0) {
            return "Hãy chọn một thể loại!";
        }
        //TODO

        if (mCurrentPosition == MAX_PAGE - 1) {
            return "Hãy điền đầy đủ thông tin";
        }
        return "Lỗi xảy ra";
    }

    private boolean canGoNext() {
        if (mCurrentPosition == 0) {
            return mFragmentSelectCategory.getSelectedCategory() != null;
        }
        return true;
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
            //TODO
            Toast.makeText(this, "TODO", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void onComplete() {
        Toast.makeText(this, "onComplete", Toast.LENGTH_SHORT).show();
    }
}
