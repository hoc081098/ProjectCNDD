package com.pkhh.projectcndd.screen.detail;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pkhh.projectcndd.R;
import com.pkhh.projectcndd.utils.Constants;
import com.pkhh.projectcndd.utils.RxFirebase;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import androidx.annotation.IntDef;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static com.pkhh.projectcndd.utils.Constants.EXTRA_MOTEL_ROOM_ID;
import static com.pkhh.projectcndd.utils.Constants.ROOMS_NAME_COLLECION;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;

public class DetailActivity extends AppCompatActivity {
  public static final int HIDE = 0;
  public static final int SHOW_NOT_SAVED = 1;
  public static final int SHOW_SAVED = 2;

  private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
  private final FirebaseAuth auth = FirebaseAuth.getInstance();
  private final CompositeDisposable compositeDisposable = new CompositeDisposable();

  @BindView(R.id.bottom_nav)
  BottomNavigationView bottomNavigationView;
  @BindView(R.id.viewpager)
  ViewPager viewPager;

  private String roomId;
  private DocumentReference roomRef;
  @SavedIconState
  private int savedIconState = HIDE;
  @Nullable
  private MenuItem prevSelectedMenuItem = null;


  @IntDef(value = {HIDE, SHOW_NOT_SAVED, SHOW_SAVED})
  @Retention(RetentionPolicy.SOURCE)
  @interface SavedIconState {
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_detail);
    ButterKnife.bind(this, this);
    getSupportActionBar().setTitle(R.string.detail);

    roomId = getIntent().getStringExtra(EXTRA_MOTEL_ROOM_ID);
    roomRef = firestore.document(Constants.ROOMS_NAME_COLLECION + "/" + roomId);

    initViews();
  }

  private void initViews() {
    requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

    final List<Fragment> fragments = Arrays.asList(
        MotelRoomDetailFragment.newInstance(roomId),
        CommentFragment.newInstance(roomId),
        RelatedFragment.newInstance(roomId)
    );

    viewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
      @Override
      public Fragment getItem(int position) {
        return fragments.get(position);
      }

      @Override
      public int getCount() {
        return fragments.size();
      }
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
    if (item.getItemId() == R.id.action_add_to_saved_or_remove_from_saved) {
      if (savedIconState == SHOW_SAVED || savedIconState == SHOW_NOT_SAVED) {
        onAddToOrRemoveFromSavedRooms();
      }
    }
    if (item.getItemId() == R.id.action_share) {
      Toast.makeText(this, "Share", Toast.LENGTH_SHORT).show();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void onAddToOrRemoveFromSavedRooms() {
    firestore.runTransaction(transaction -> {
      final FirebaseUser currentUser = auth.getCurrentUser();
      if (currentUser == null) {
        throw new IllegalStateException(getString(R.string.you_must_login_to_perform_this_function));
      }

      final String uid = currentUser.getUid();
      final DocumentReference document = firestore.collection(ROOMS_NAME_COLLECION).document(roomId);

      Map<?, ?> userIdsSaved = (Map<?, ?>) transaction.get(document).get("user_ids_saved");
      userIdsSaved = userIdsSaved == null ? emptyMap() : userIdsSaved;

      if (userIdsSaved.containsKey(uid)) {

        transaction.update(document, "user_ids_saved." + uid, FieldValue.serverTimestamp());
        return R.string.remove_from_saved_list_successfully;

      } else {

        transaction.update(document, "user_ids_saved." + uid, FieldValue.delete());
        return R.string.add_to_saved_list_successfully;

      }
    }).addOnSuccessListener(this,
        msg -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show())
        .addOnFailureListener(this,
            e -> Toast.makeText(this, getString(R.string.error, e.getMessage()), Toast.LENGTH_SHORT).show());
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    final MenuItem menuItem = menu.findItem(R.id.action_add_to_saved_or_remove_from_saved);
    if (menuItem != null) {
      switch (savedIconState) {
        case HIDE:
          menuItem.setVisible(false);
          break;
        case SHOW_NOT_SAVED:
          menuItem.setVisible(true);
          menuItem.setIcon(R.drawable.ic_bookmark_border_grey_24dp);
          menuItem.setTitle(R.string.add_saved);
          break;
        case SHOW_SAVED:
          menuItem.setVisible(true);
          menuItem.setIcon(R.drawable.ic_bookmark_black_24dp);
          menuItem.setTitle(R.string.remove_saved);
          break;
      }
    }
    return true;
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.detail_menu, menu);
    return true;
  }

  @Override
  protected void onStart() {
    super.onStart();

    final Observable<FirebaseAuth> firebaseAuthObservable = RxFirebase
        .authStateChanges(auth)
        .subscribeOn(Schedulers.io());
    final Observable<Map<?, ?>> idsSaved = RxFirebase
        .documentSnapshots(roomRef)
        .subscribeOn(Schedulers.io())
        .map(snapshot -> {
          Map<?, ?> userIdsSaved = (Map<?, ?>) snapshot.get("user_ids_saved");
          return userIdsSaved == null ? emptyMap() : userIdsSaved;
        });

    compositeDisposable.add(
        Observable
            .combineLatest(idsSaved, firebaseAuthObservable, Pair<Map<?, ?>, FirebaseAuth>::new)
            .subscribeOn(Schedulers.io())
            .map(pair -> {
              final Map<?, ?> ids = requireNonNull(pair.first);
              final FirebaseAuth auth = requireNonNull(pair.second);
              @Nullable final FirebaseUser currentUser = auth.getCurrentUser();
              if (currentUser == null) {
                return HIDE;
              }
              if (ids.containsKey(currentUser.getUid())) {
                return SHOW_SAVED;
              }
              return SHOW_NOT_SAVED;
            })
            .distinctUntilChanged()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(savedIconState -> {
              this.savedIconState = savedIconState;
              invalidateOptionsMenu();
            }, e -> {
              Toast.makeText(this, getString(R.string.error, e.getMessage()), Toast.LENGTH_SHORT).show();
            })
    );
  }

  @Override
  protected void onStop() {
    super.onStop();
    compositeDisposable.clear();
  }
}
