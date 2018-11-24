package com.pkhh.projectcndd.screen.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.pkhh.projectcndd.CommonRoomVH;
import com.pkhh.projectcndd.R;
import com.pkhh.projectcndd.models.FirebaseModel;
import com.pkhh.projectcndd.models.MotelRoom;
import com.pkhh.projectcndd.screen.detail.MotelRoomDetailActivity;
import com.pkhh.projectcndd.utils.Constants;
import com.pkhh.projectcndd.utils.SharedPrefUtil;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.ButterKnife;

import static com.pkhh.projectcndd.screen.home.HomeAdapter.QUERY_DIRECTION;
import static com.pkhh.projectcndd.screen.home.SeeAll.COUNT_VIEW_DESCENDING;
import static com.pkhh.projectcndd.screen.home.SeeAll.CREATED_AT_DESCENDING;
import static com.pkhh.projectcndd.utils.Constants.MOTEL_ROOM_ID;
import static com.pkhh.projectcndd.utils.Constants.PROVINCES_NAME_COLLECION;
import static com.pkhh.projectcndd.utils.Constants.ROOMS_NAME_COLLECION;
import static java.util.Objects.requireNonNull;

public class ShowMoreActivity extends AppCompatActivity {
  public static final int PAGE_SIZE = 20;
  private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

  private MyFirebaseLoadMoreAdapter<MotelRoom, RecyclerView.ViewHolder> adapter;

  @BindView(R.id.recycler) RecyclerView recyclerView;
  @BindView(R.id.swipe_refresh_layout) SwipeRefreshLayout swipeRefreshLayout;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_show_more);

    ButterKnife.bind(this, this);
    requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

    swipeRefreshLayout.post(() -> swipeRefreshLayout.setRefreshing(true));
    swipeRefreshLayout.setOnRefreshListener(() -> {
      if (adapter != null) adapter.refresh();
    });

    final int queryDir = getIntent().getIntExtra(QUERY_DIRECTION, 0);
    final String selectedProvinceId = SharedPrefUtil.getInstance(this).getSelectedProvinceId(getString(R.string.da_nang_id));
    final DocumentReference selectedProvinceRef = firestore.document(PROVINCES_NAME_COLLECION + "/" + selectedProvinceId);

    if (queryDir == CREATED_AT_DESCENDING) {
      final Query query = firestore.collection(Constants.ROOMS_NAME_COLLECION)
          .whereEqualTo("province", selectedProvinceRef)
          .whereEqualTo("is_active", true)
          .orderBy("created_at", Query.Direction.DESCENDING);

      getSupportActionBar().setTitle("Mới nhất");
      setupRecyclerView(query);

    } else if (queryDir == COUNT_VIEW_DESCENDING) {
      final Query query = firestore.collection(ROOMS_NAME_COLLECION)
          .whereEqualTo("province", selectedProvinceRef)
          .whereEqualTo("is_active", true)
          .orderBy("count_view", Query.Direction.DESCENDING);

      getSupportActionBar().setTitle("Xem nhiều");
      setupRecyclerView(query);
    }
  }

  private void setupRecyclerView(Query query) {
    recyclerView.setHasFixedSize(true);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));

    adapter = new MyFirebaseLoadMoreAdapter<MotelRoom, RecyclerView.ViewHolder>(
        query,
        MotelRoom.class,
        snapshot -> FirebaseModel.documentSnapshotToObject(snapshot, MotelRoom.class),
        PAGE_SIZE,
        PAGE_SIZE / 2,
        recyclerView
    ) {

      class LoadMoreVH extends RecyclerView.ViewHolder {
        LoadMoreVH(@NonNull View itemView) { super(itemView); }
      }

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

          final Intent intent = new Intent(ShowMoreActivity.this, MotelRoomDetailActivity.class);
          intent.putExtra(MOTEL_ROOM_ID, room.getId());

          startActivity(intent);
        }
      }

      @Override
      protected void onFirstLoaded() { swipeRefreshLayout.setRefreshing(false); }
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

}
