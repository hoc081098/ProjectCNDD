package com.pkhh.projectcndd.ui.home;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.annimon.stream.Stream;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.pkhh.projectcndd.R;
import com.pkhh.projectcndd.models.FirebaseModel;
import com.pkhh.projectcndd.models.MotelRoom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.transition.Fade;
import androidx.transition.Slide;
import androidx.transition.TransitionManager;
import androidx.transition.TransitionSet;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.pkhh.projectcndd.utils.Constants.MOTEL_ROOM_NAME_COLLECION;

public class MotelRoomsListFragment extends Fragment {
  public static final String TAG = MotelRoomsListFragment.class.getSimpleName();
  private static final int PAGE_SIZE = 30;

  private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
  private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

  private HomeAdapter adapter;
  private List<HomeListItem> homeListItems = new ArrayList<>();

  @BindView(R.id.root_motel_rooms_list_fragment)
  ViewGroup rootLayout;

  @BindView(R.id.swipe_refresh_layout)
  SwipeRefreshLayout swipeRefreshLayout;

  @BindView(R.id.loading_layout)
  ViewGroup loadingLayout;

  private Unbinder unbinder;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_motel_rooms_list, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    unbinder = ButterKnife.bind(this, view);

    setupRecyclerViewAndAdapter(view);

    swipeRefreshLayout.setOnRefreshListener(() -> loadData(false));
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    unbinder.unbind();
  }

  /*private void onItemClick(int viewId, @NonNull MotelRoom roomItem, int position) {
    if (viewId == R.id.image_share) {
      Toast.makeText(requireContext(), "Share clicked", Toast.LENGTH_SHORT).show();
      onShareClicked(roomItem.getId());
      return;
    }

    if (viewId == R.id.image_save) {
      onSaveClicked(roomItem.getId(), position);
      return;
    }

    Intent intent = new Intent(requireContext(), MotelRoomDetailActivity.class);
    intent.putExtra(MOTEL_ROOM_ID, roomItem.getId());
    startActivity(intent);
  }

  private void onSaveClicked(String id, int position) {
    FirebaseFirestore.getInstance().runTransaction(transaction -> {
      final FirebaseUser currentUser = firebaseAuth.getCurrentUser();
      if (currentUser == null) {
        throw new IllegalStateException("Bạn phải login mới thưc hiện được chức năng này!");
      }
      final String uid = currentUser.getUid();

      final DocumentReference document = firestore
          .collection(MOTEL_ROOM_NAME_COLLECION)
          .document(id);

      final MotelRoom item = FirebaseModel.documentSnapshotToObject(transaction.get(document), MotelRoom.class);
      final List<String> userIdsSaved = item.getUserIdsSaved();

      if (userIdsSaved.contains(uid)) {
        transaction.update(document, "user_ids_saved", FieldValue.arrayRemove(uid));
        userIdsSaved.remove(uid);
      } else {
        transaction.update(document, "user_ids_saved", FieldValue.arrayUnion(uid));
        userIdsSaved.add(uid);
      }

      item.setUserIdsSaved(userIdsSaved);
      return item;
    }).addOnSuccessListener(item -> {

      adapter.getList().set(position, item);
      adapter.notifyItemChanged(position);
      Snackbar.make(rootLayout, "Done", Snackbar.LENGTH_SHORT).show();

    }).addOnFailureListener(e -> {
      Snackbar.make(rootLayout, "Error: " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
    });
  }

  private void onShareClicked(String modelId) {

  }*/


  private void setupRecyclerViewAndAdapter(@NonNull View view) {
    adapter = new HomeAdapter();

    RecyclerView recyclerView = view.findViewById(R.id.recycler);
    recyclerView.setHasFixedSize(true);

    final GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 2);
    layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
      @Override
      public int getSpanSize(int position) {
        if (adapter.getItemViewType(position) == R.layout.home_room_item_layout) {
          return 1;
        }
        return 2;
      }
    });

    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setAdapter(adapter);
    final int space = 4;
    recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
      @Override
      public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        final int position = parent.getChildLayoutPosition(view);
        if (adapter.getItemViewType(position) == R.layout.home_room_item_layout) {
          outRect.left = space;
          outRect.right = space;
          outRect.bottom = space;
          outRect.top = space;
        }
      }
    });

    loadData(true);
  }

  private void loadData(boolean isFirstLoad) {
    homeListItems.clear();

    homeListItems.add(
        new BannerItem(
            Arrays.asList(
                new ImageAndDescriptionBanner("https://images.unsplash.com/photo-1504333638930-c8787321eee0?ixlib=rb-0.3.5&s=8fa53af55b1f12c07d2c3d4c1358c20a&w=1000&q=80", "Image 1"),
                new ImageAndDescriptionBanner("https://images.unsplash.com/photo-1483356256511-b48749959172?ixlib=rb-0.3.5&ixid=eyJhcHBfaWQiOjEyMDd9&s=fbb470799cbb69de82bc80822eee3255&w=1000&q=80", "Image 2"),
                new ImageAndDescriptionBanner("http://file.vforum.vn/hinh/2015/11/vforum.vn-hinh-anh-nen-ngan-ha-vu-tru-bao-la-10.jpg", "Image 3")
            )
        )
    );

    firestore.collection(MOTEL_ROOM_NAME_COLLECION)
        .orderBy("created_at", Query.Direction.DESCENDING)
        .limit(20)
        .get()
        .addOnSuccessListener(queryDocumentSnapshots -> {

          homeListItems.add(new HeaderItem("Mới nhất"));
          homeListItems.addAll(
              Stream.of(FirebaseModel.querySnapshotToObjects(queryDocumentSnapshots, MotelRoom.class))
                  .map(motelRoom -> new RoomItem(motelRoom.getId(), motelRoom.getTitle(), motelRoom.getAddress(), motelRoom.getPrice(), motelRoom.getImages(), motelRoom.getDistrict()))
                  .toList()
          );
          homeListItems.add(new SeeAll(QueryDirection.CREATED_AT_DESCENDING));

          firestore.collection(MOTEL_ROOM_NAME_COLLECION)
              .orderBy("view_count", Query.Direction.DESCENDING)
              .limit(20)
              .get()
              .addOnSuccessListener(queryDocumentSnapshots1 -> {

                homeListItems.add(new HeaderItem("Xem nhiều"));
                homeListItems.addAll(
                    Stream.of(FirebaseModel.querySnapshotToObjects(queryDocumentSnapshots1, MotelRoom.class))
                        .map(motelRoom -> new RoomItem(motelRoom.getId(), motelRoom.getTitle(), motelRoom.getAddress(), motelRoom.getPrice(), motelRoom.getImages(), motelRoom.getDistrict()))
                        .toList()
                );
                homeListItems.add(new SeeAll(QueryDirection.VIEW_COUNT_DESCENDING));

                adapter.submitList(new ArrayList<>(homeListItems));

                if (isFirstLoad) {
                  TransitionManager.beginDelayedTransition(rootLayout, new TransitionSet()
                      .addTransition(new Fade(Fade.OUT))
                      .addTransition(new Slide(Gravity.END))
                      .setDuration(500)
                      .addTarget(loadingLayout)
                  );
                  loadingLayout.setVisibility(View.INVISIBLE);
                } else {
                  Toast.makeText(requireContext(), "Refresh successfully", Toast.LENGTH_SHORT).show();
                  if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                  }
                }
              });
        });
  }
}