package com.pkhh.projectcndd.screen.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Source;
import com.pkhh.projectcndd.CommonRoomVH;
import com.pkhh.projectcndd.R;
import com.pkhh.projectcndd.models.FirebaseModel;
import com.pkhh.projectcndd.models.MotelRoom;
import com.pkhh.projectcndd.screen.detail.MotelRoomDetailActivity;
import com.pkhh.projectcndd.utils.Constants;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

import static com.firebase.ui.firestore.paging.LoadingState.ERROR;
import static com.firebase.ui.firestore.paging.LoadingState.FINISHED;
import static com.firebase.ui.firestore.paging.LoadingState.LOADED;
import static com.firebase.ui.firestore.paging.LoadingState.LOADING_INITIAL;
import static com.firebase.ui.firestore.paging.LoadingState.LOADING_MORE;
import static com.pkhh.projectcndd.utils.Constants.MOTEL_ROOM_ID;
import static com.pkhh.projectcndd.utils.Constants.PROVINCES_NAME_COLLECION;
import static com.pkhh.projectcndd.utils.Constants.ROOMS_NAME_COLLECION;

public class ShowMoreActivity extends AppCompatActivity {
  private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

  private final String selectedProvinceId = "NtHjwobYdIi0YwTUHz05"; // TODO: user can change selected province
  private final DocumentReference selectedProvinceRef = firestore.document(PROVINCES_NAME_COLLECION + "/" + selectedProvinceId);

  public static final int PAGE_SIZE = 20;
  private FirestorePagingAdapter<MotelRoom, CommonRoomVH> adapter;

  @BindView(R.id.recycler) RecyclerView recyclerView;
  @BindView(R.id.progress_bar) ProgressBar progressBar;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_show_more);
    ButterKnife.bind(this, this);
    Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

    final int queryDir = getIntent().getIntExtra(HomeAdapter.QUERY_DIRECTION, 0);

    Query query = null;
    if (queryDir == SeeAll.CREATED_AT_DESCENDING) {
      query = firestore.collection(Constants.ROOMS_NAME_COLLECION)
          .whereEqualTo("province", selectedProvinceRef)
          .whereEqualTo("is_active", true)
          .orderBy("created_at", Query.Direction.DESCENDING);

      getSupportActionBar().setTitle("Mới nhất");
    } else if (queryDir == SeeAll.COUNT_VIEW_DESCENDING) {
      query = firestore.collection(ROOMS_NAME_COLLECION)
          .whereEqualTo("province", selectedProvinceRef)
          .whereEqualTo("is_active", true)
          .orderBy("count_view", Query.Direction.DESCENDING);

      getSupportActionBar().setTitle("Xem nhiều");
    }
    setupRecyclerView(query);
  }

  private void setupRecyclerView(Query query) {
    final PagedList.Config config = new PagedList.Config.Builder()
        .setEnablePlaceholders(false)
        .setInitialLoadSizeHint(PAGE_SIZE)
        .setPageSize(PAGE_SIZE)
        .setPrefetchDistance(PAGE_SIZE)
        .build();

    final FirestorePagingOptions<MotelRoom> options = new FirestorePagingOptions.Builder<MotelRoom>()
        .setQuery(query, Source.SERVER, config, snapshot -> FirebaseModel.documentSnapshotToObject(snapshot, MotelRoom.class))
        .build();

    adapter = new FirestorePagingAdapter<MotelRoom, CommonRoomVH>(options) {

      @NonNull
      @Override
      public CommonRoomVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.common_room_item_layout, parent, false);
        return new CommonRoomVH(itemView, this::onItemClick);
      }

      private void onItemClick(View __, int position) {
        final DocumentSnapshot snapshot = getItem(position);
        if (snapshot != null) {
          final Intent intent = new Intent(ShowMoreActivity.this, MotelRoomDetailActivity.class);
          intent.putExtra(MOTEL_ROOM_ID, snapshot.getId());
          startActivity(intent);
        }
      }

      @Override
      protected void onBindViewHolder(@NonNull CommonRoomVH VH, int i, @NonNull MotelRoom motelRoom) {
        VH.bind(motelRoom);
      }

      @Override
      protected void onLoadingStateChanged(@NonNull LoadingState state) {
        super.onLoadingStateChanged(state);
        if (state == LOADED || state == FINISHED || state == ERROR) {
          progressBar.setVisibility(View.INVISIBLE);
        }

        if (state == LOADING_INITIAL || state == LOADING_MORE) {
          progressBar.setVisibility(View.VISIBLE);
        }
      }
    };

    recyclerView.setHasFixedSize(true);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    recyclerView.setAdapter(adapter);

    adapter.startListening();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    adapter.stopListening();
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
