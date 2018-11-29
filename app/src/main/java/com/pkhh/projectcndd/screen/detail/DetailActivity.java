package com.pkhh.projectcndd.screen.detail;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.pkhh.projectcndd.R;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import butterknife.ButterKnife;

import static com.pkhh.projectcndd.utils.Constants.EXTRA_MOTEL_ROOM_ID;

public class DetailActivity extends AppCompatActivity {

  @BindView(R.id.bottom_nav) BottomNavigationView bottomNavigationView;
  @BindView(R.id.viewpager) ViewPager viewPager;


  private String roomId;
  @Nullable private MenuItem prevSelectedMenuItem = null;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_detail);
    ButterKnife.bind(this, this);

    roomId = getIntent().getStringExtra(EXTRA_MOTEL_ROOM_ID);

    initViews();
  }

  private void initViews() {
    Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

    final List<Fragment> fragments = Arrays.asList(
        MotelRoomDetailFragment.newInstance(roomId),
        CommentFragment.newInstance(roomId),
        RelatedFragment.newInstance(roomId)
    );

    viewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
      @Override
      public Fragment getItem(int position) { return fragments.get(position); }

      @Override
      public int getCount() { return fragments.size(); }
    });

    bottomNavigationView.setOnNavigationItemSelectedListener(menuItem -> {
      switch (menuItem.getItemId()) {
        case R.id.action_detail:
          viewPager.setCurrentItem(0, true);
          break;
        case R.id.action_comment:
          viewPager.setCurrentItem(1, true);
          break;
        case R.id.action_related:
          viewPager.setCurrentItem(2, true);
          break;
      }
      return false;
    });

    viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
      @Override
      public void onPageSelected(int position) {
        super.onPageSelected(position);
        if (prevSelectedMenuItem != null) {
          prevSelectedMenuItem.setChecked(false);
        } else {
          bottomNavigationView.getMenu().getItem(0).setChecked(false);
        }

        final MenuItem item = bottomNavigationView.getMenu().getItem(position);
        item.setChecked(true);
        prevSelectedMenuItem = item;
      }
    });

    viewPager.setOffscreenPageLimit(fragments.size());
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      onBackPressed();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }
}
