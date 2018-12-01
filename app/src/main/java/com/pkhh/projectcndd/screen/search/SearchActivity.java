package com.pkhh.projectcndd.screen.search;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.appyvet.materialrangebar.RangeBar;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.pkhh.projectcndd.CommonRoomVH;
import com.pkhh.projectcndd.R;
import com.pkhh.projectcndd.models.FirebaseModel;
import com.pkhh.projectcndd.models.MotelRoom;
import com.pkhh.projectcndd.screen.detail.DetailActivity;
import com.pkhh.projectcndd.screen.home.MyFirebaseLoadMoreAdapter;
import com.pkhh.projectcndd.utils.Constants;
import com.pkhh.projectcndd.utils.SharedPrefUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

import static com.pkhh.projectcndd.utils.Constants.EXTRA_MOTEL_ROOM_ID;
import static java.util.Objects.requireNonNull;

public class SearchActivity extends AppCompatActivity {
  public static final int PAGE_SIZE = 20;

  @BindView(R.id.toolbar) Toolbar toolbar;
  @BindView(R.id.empty_layout) ConstraintLayout emptyLayout;
  @BindView(R.id.progressbar) ProgressBar progressbar;
  @BindView(R.id.recycler_search) RecyclerView recyclerView;

  @BindView(R.id.price_rangebar) RangeBar priceRangebar;
  @BindView(R.id.filter_bottom_sheet) ConstraintLayout filterBottomSheet;
  @BindView(R.id.buttonFilter) AppCompatTextView buttonFilter;
  @BindView(R.id.spinnerLimit) Spinner spinnerLimit;
  @BindView(R.id.spinnerSelectDistrict) Spinner spinnerSelectDistrict;
  @BindView(R.id.spinnerSortDate) Spinner spinnerSortDate;
  @BindView(R.id.spinnerSortPrice) Spinner spinnerSortPrice;
  @BindView(R.id.spinnerSortViewCount) Spinner spinnerSortViewCount;
  @BindView(R.id.spinnerSelectCategory) Spinner spinnerSelectCategory;

  private long maxPrice = 0;
  private long minPrice = 0;

  private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
  private MyFirebaseLoadMoreAdapter<MotelRoom, RecyclerView.ViewHolder> adapter;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_search);
    ButterKnife.bind(this, this);

    setSupportActionBar(toolbar);
    requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    requireNonNull(getSupportActionBar()).setTitle(R.string.search);

    recyclerView.setHasFixedSize(true);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));

    setupAdapter(firestore.collection(Constants.ROOMS_NAME_COLLECION));
    setupFilterBottomSheet();
  }

  private void setupFilterBottomSheet() {
    final BottomSheetBehavior<ConstraintLayout> bottomSheetBehavior = BottomSheetBehavior.from(filterBottomSheet);
    final Drawable upDrawable = ContextCompat.getDrawable(this, R.drawable.ic_keyboard_arrow_up_black_24dp);
    final Drawable downDrawable = ContextCompat.getDrawable(this, R.drawable.ic_keyboard_arrow_down_black_24dp);
    bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
      @Override
      public void onStateChanged(@NonNull View view, int state) {
        if (state == BottomSheetBehavior.STATE_COLLAPSED || state == BottomSheetBehavior.STATE_EXPANDED) {
          TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(
              buttonFilter,
              null,
              null,
              state == BottomSheetBehavior.STATE_COLLAPSED ? upDrawable : downDrawable,
              null
          );
        }
      }

      @Override
      public void onSlide(@NonNull View view, float v) { }
    });
    buttonFilter.setOnClickListener(__ -> {
      switch (bottomSheetBehavior.getState()) {
        case BottomSheetBehavior.STATE_COLLAPSED:
          bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
          break;
        case BottomSheetBehavior.STATE_EXPANDED:
          bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
          break;
        case BottomSheetBehavior.STATE_DRAGGING:
          break;
        case BottomSheetBehavior.STATE_HALF_EXPANDED:
          break;
        case BottomSheetBehavior.STATE_HIDDEN:
          break;
        case BottomSheetBehavior.STATE_SETTLING:
          break;
      }
    });
    setupRangeSeekBar();

    List<Long> limits = new ArrayList<>((500 - 10) / 10 + 1);
    for (long i = 10L; i <= 500L; i += 10L) {
      limits.add(i);
    }
    spinnerLimit.setAdapter(
        new ArrayAdapter<>(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            limits
        )
    );
    spinnerLimit.setSelection(0);

    final ArrayAdapter<String> adapterSortDirection = new ArrayAdapter<>(
        this,
        android.R.layout.simple_spinner_dropdown_item,
        Arrays.asList(getString(R.string.asc), getString(R.string.desc))
    );
    spinnerSortDate.setAdapter(adapterSortDirection);
    spinnerSortPrice.setAdapter(adapterSortDirection);
    spinnerSortViewCount.setAdapter(adapterSortDirection);
    spinnerSortDate.setSelection(0);
    spinnerSortPrice.setSelection(0);
    spinnerSortViewCount.setSelection(0);

    firestore.collection(Constants.PROVINCES_NAME_COLLECION + "/"
        + SharedPrefUtil.getInstance(this).getSelectedProvinceId(getString(R.string.da_nang_id))
        + "/" + Constants.DISTRICTS_NAME_COLLECION)
        .orderBy("name")
        .get()
        .addOnSuccessListener(this, queryDocumentSnapshots -> {
          final List<District> districts = FirebaseModel.querySnapshotToObjects(queryDocumentSnapshots, District.class);
          spinnerSelectDistrict.setAdapter(
              new ArrayAdapter<>(
                  SearchActivity.this,
                  android.R.layout.simple_spinner_dropdown_item,
                  districts
              )
          );
          spinnerSelectDistrict.setSelection(0);
          Timber.tag("@@@@").d("District = %s", districts);
        })
        .addOnFailureListener(this, e -> {});

    firestore.collection(Constants.CATEGORIES_NAME_COLLECION)
        .orderBy("name")
        .get()
        .addOnSuccessListener(this, queryDocumentSnapshots -> {
          final List<Category> categories = FirebaseModel.querySnapshotToObjects(queryDocumentSnapshots, Category.class);
          spinnerSelectCategory.setAdapter(
              new ArrayAdapter<>(
                  SearchActivity.this,
                  android.R.layout.simple_spinner_dropdown_item,
                  categories
              )
          );
          spinnerSelectCategory.setSelection(0);
          Timber.tag("@@@@").d("categories = %s", categories);
        })
        .addOnFailureListener(this, e -> {});
  }

  private void setupRangeSeekBar() {
    minPrice = 100_000 / 1_000;
    maxPrice = 10_000_000 / 1_000;

    priceRangebar.setPinTextColor(ContextCompat.getColor(this, android.R.color.white));
    priceRangebar.setTickStart(minPrice);
    priceRangebar.setTickEnd(maxPrice);
    priceRangebar.setTickInterval(100);

    Tasks.whenAllSuccess(
        firestore
            .collection(Constants.ROOMS_NAME_COLLECION)
            .orderBy("price", Query.Direction.ASCENDING)
            .limit(1)
            .get(),
        firestore
            .collection(Constants.ROOMS_NAME_COLLECION)
            .orderBy("price", Query.Direction.DESCENDING)
            .limit(1)
            .get()
    ).addOnSuccessListener(this, querySnapshots -> {
      final QuerySnapshot minSnapshot = (QuerySnapshot) querySnapshots.get(0);
      final QuerySnapshot maxSnapshot = (QuerySnapshot) querySnapshots.get(1);

      Long minPrice = requireNonNull(minSnapshot.getDocuments().get(0).getLong("price"));
      Long maxPrice = requireNonNull(maxSnapshot.getDocuments().get(0).getLong("price"));
      minPrice = (minPrice / 100_000) * 100_000 / 1_000;
      maxPrice = (long) (Math.ceil(maxPrice / 100_000D) * 100_000 / 1_000);

      Timber.tag("@@@@@").d("min = %s", minPrice);
      Timber.tag("@@@@@").d("max = %s", maxPrice);

      priceRangebar.setTickStart(minPrice);
      priceRangebar.setTickEnd(maxPrice);
      priceRangebar.setTickInterval(100);

      Timber.tag("@@@@@").d("Setup range Get max and min price successfully " + minPrice + ", " + maxPrice);
      Timber.tag("@@@@@").d("Get max and min price successfully " + minPrice + ", " + maxPrice);
    }).addOnFailureListener(this, e -> Toast.makeText(this, "Get max and min price error: " + e.getMessage(), Toast.LENGTH_SHORT).show());


    priceRangebar.setOnRangeBarChangeListener((__, ___, ____, leftPinValue, rightPinValue) -> {
      minPrice = (long) toFloatOrZero(leftPinValue);
      maxPrice = (long) toFloatOrZero(rightPinValue);
      Timber.tag("@@@@@").d("Change " + minPrice + ", " + maxPrice);
    });
  }

  private float toFloatOrZero(String s) {
    try {
      return Float.parseFloat(s);
    } catch (NumberFormatException e) {
      return 0f;
    }
  }

  private void setupAdapter(Query query) {
    progressbar.setVisibility(View.VISIBLE);
    emptyLayout.setVisibility(View.INVISIBLE);

    adapter = new MyFirebaseLoadMoreAdapter<MotelRoom, RecyclerView.ViewHolder>(
        query,
        MotelRoom.class,
        snapshot -> FirebaseModel.documentSnapshotToObject(snapshot, MotelRoom.class),
        PAGE_SIZE,
        PAGE_SIZE / 2,
        recyclerView
    ) {

      @NonNull
      @Override
      public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_FIREBASE_MODEL_ITEM) {
          View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.common_room_item_layout, parent, false);
          return new CommonRoomVH(itemView, this::onItemClick);
        }
        if (viewType == TYPE_LOAD_MORE) {
          View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.load_more_item_layout, parent, false);
          return new LoadMoreVH(itemView);
        }
        throw new IllegalStateException("Don't know viewType = " + viewType);
      }

      @Override
      public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final Object item = getItem(position);
        if (holder instanceof CommonRoomVH && item instanceof MotelRoom) {
          ((CommonRoomVH) holder).bind((MotelRoom) item);
        }
      }

      private void onItemClick(View __, int position) {
        final Object item = getItem(position);
        if (item instanceof MotelRoom) {
          final MotelRoom room = (MotelRoom) item;

          final Intent intent = new Intent(SearchActivity.this, DetailActivity.class);
          intent.putExtra(EXTRA_MOTEL_ROOM_ID, room.getId());

          startActivity(intent);
        }
      }

      @Override
      protected void onFirstLoaded() { progressbar.setVisibility(View.INVISIBLE); }

      @Override
      protected void onDataChanged() {
        super.onDataChanged();
        progressbar.setVisibility(View.INVISIBLE);
        if (getItemCount() == 0) {
          emptyLayout.setVisibility(View.VISIBLE);
          recyclerView.setVisibility(View.INVISIBLE);
        } else {
          recyclerView.setVisibility(View.VISIBLE);
          emptyLayout.setVisibility(View.INVISIBLE);
        }
      }

      class LoadMoreVH extends RecyclerView.ViewHolder {
        LoadMoreVH(@NonNull View itemView) { super(itemView); }
      }
    };

    recyclerView.setAdapter(adapter);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      onBackPressed();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private Query.Direction toDirection(String s) {
    if (getString(R.string.asc).equals(s)) {
      return Query.Direction.ASCENDING;
    }
    if (getString(R.string.desc).equals(s)) {
      return Query.Direction.DESCENDING;
    }
    throw new IllegalStateException("Not found direction");
  }

  @OnClick({R.id.button_apply_filter})
  public void performSearch(View v) {
    final long limit = (long) spinnerLimit.getSelectedItem();
    final District district = (District) spinnerSelectDistrict.getSelectedItem();
    final Query.Direction sortDate = toDirection((String) spinnerSortDate.getSelectedItem());
    final Query.Direction sortPrice = toDirection((String) spinnerSortPrice.getSelectedItem());
    final Query.Direction sortViewCount = toDirection((String) spinnerSortViewCount.getSelectedItem());
    final Category category = (Category) spinnerSelectCategory.getSelectedItem();
    final long minPrice = this.minPrice * 1_000;
    final long maxPrice = this.maxPrice * 1_000;

    final String test = String.format("%d %d %d %s %s %s %s", minPrice, maxPrice, limit, district, sortDate, sortPrice, sortViewCount);
    Timber.tag("@@@@@").d(test);

    final Query query = firestore
        .collection(Constants.ROOMS_NAME_COLLECION)
        .whereEqualTo("approve", true)
        .whereEqualTo("category", firestore.collection(Constants.CATEGORIES_NAME_COLLECION) + "/" + category.getId())
        .whereEqualTo("district", firestore.collection(Constants.PROVINCES_NAME_COLLECION + "/"
        + SharedPrefUtil.getInstance(this).getSelectedProvinceId(getString(R.string.da_nang_id))
        + "/" + Constants.DISTRICTS_NAME_COLLECION + "/" + district.getId()))
        .whereGreaterThanOrEqualTo("price", minPrice)
        .whereLessThanOrEqualTo("price", maxPrice)
        .orderBy("price", sortPrice)
        .orderBy("count_view", sortViewCount)
        .orderBy("created_at", sortDate)
        .limit(limit);
    setupAdapter(query);
  }

  static class District extends FirebaseModel {
    final String name;

    District() { this(""); }

    District(String name) { this.name = name; }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      District district = (District) o;
      return Objects.equals(id, district.id) && Objects.equals(name, district.name);
    }

    @Override
    public int hashCode() {
      return Objects.hash(id, name);
    }

    @Override
    public String toString() {
      return name;
    }
  }

  static class Category extends FirebaseModel {
    final String name;

    Category() { this(""); }

    Category(String name) { this.name = name; }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Category district = (Category) o;
      return Objects.equals(id, district.id) && Objects.equals(name, district.name);
    }

    @Override
    public int hashCode() {
      return Objects.hash(id, name);
    }

    @Override
    public String toString() {
      return name;
    }
  }
}
