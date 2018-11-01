package com.pkhh.projectcndd.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

import static com.pkhh.projectcndd.utils.Constants.MOTEL_ROOM_NAME_COLLECION;
import static java.util.Objects.requireNonNull;

public class MotelRoomsListFragment extends Fragment {
  public static final String TAG = MotelRoomsListFragment.class.getSimpleName();
  private static final int PAGE_SIZE = 30;

  private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
  private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

  private ViewGroup rootLayout;
  private HomeAdapter adapter;
  private List<HomeListItem> homeListItems = new ArrayList<>();
  //  private SwipeRefreshLayout swipeRefreshLayout;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_motel_rooms_list, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    setupRecyclerViewAndAdapter(view);

    rootLayout = view.findViewById(R.id.root_motel_rooms_list_fragment);
//    swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
//    swipeRefreshLayout.setOnRefreshListener(() -> {
//      if (adapter != null) {
//        adapter.refresh();
//      }
//    });
//    swipeRefreshLayout.post(() -> swipeRefreshLayout.setRefreshing(true));

    FirebaseFirestore.setLoggingEnabled(true);
  }
/*
  private void onItemClick(int viewId, @NonNull MotelRoom roomItem, int position) {
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
    RecyclerView recyclerView = view.findViewById(R.id.recycler);
    recyclerView.setHasFixedSize(true);
    final GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 2);
    recyclerView.setLayoutManager(layoutManager);
    layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
      @Override
      public int getSpanSize(int position) {
        final HomeListItem item = homeListItems.get(position);
        if (item instanceof RoomItem) {
          return 1;
        }
        return 2;
      }
    });

    adapter = new HomeAdapter();
    recyclerView.setAdapter(adapter);

    homeListItems.add(
        new BannerItem(
            Arrays.asList(
                new ImageAndDescriptionBanner("https://images.unsplash.com/photo-1504333638930-c8787321eee0?ixlib=rb-0.3.5&s=8fa53af55b1f12c07d2c3d4c1358c20a&w=1000&q=80", "Image 1"),
                new ImageAndDescriptionBanner("https://images.unsplash.com/photo-1483356256511-b48749959172?ixlib=rb-0.3.5&ixid=eyJhcHBfaWQiOjEyMDd9&s=fbb470799cbb69de82bc80822eee3255&w=1000&q=80", "Image 2"),
                new ImageAndDescriptionBanner("http://file.vforum.vn/hinh/2015/11/vforum.vn-hinh-anh-nen-ngan-ha-vu-tru-bao-la-10.jpg", "Image 3")
            )
        )
    );
    adapter.submitList(homeListItems = new ArrayList<>(homeListItems));

    firestore.collection(MOTEL_ROOM_NAME_COLLECION)
        .orderBy("created_at", Query.Direction.DESCENDING)
        .limit(6)
        .get()
        .addOnSuccessListener(queryDocumentSnapshots -> {

          homeListItems.add(new HeaderItem("Mới nhất"));
          homeListItems.addAll(
              Stream.of(FirebaseModel.querySnapshotToObjects(queryDocumentSnapshots, MotelRoom.class))
                  .map(motelRoom -> new RoomItem(motelRoom.getId(), motelRoom.getTitle(), motelRoom.getAddress(), motelRoom.getPrice()))
                  .toList()
          );
          adapter.submitList(homeListItems = new ArrayList<>(homeListItems));


          firestore.collection(MOTEL_ROOM_NAME_COLLECION)
              .orderBy("view_count", Query.Direction.DESCENDING)
              .limit(6)
              .get()
              .addOnSuccessListener(queryDocumentSnapshots1 -> {
                homeListItems.add(new HeaderItem("Xem nhiều"));
                homeListItems.addAll(
                    Stream.of(FirebaseModel.querySnapshotToObjects(queryDocumentSnapshots1, MotelRoom.class))
                        .map(motelRoom -> new RoomItem(motelRoom.getId(), motelRoom.getTitle(), motelRoom.getAddress(), motelRoom.getPrice()))
                        .toList()
                );
                adapter.submitList(homeListItems = new ArrayList<>(homeListItems));
              });

        });


  }
}
/*


class X extends MyFirebaseLoadMoreAdapter<MotelRoom> {

  public X(Query query, int pageSize, RecyclerView recyclerView, Class<MotelRoom> motelRoomClass) {
    super(query, pageSize, recyclerView, motelRoomClass);
  }

  @Override
  protected void onLastItemReached() {
    Snackbar.make(rootLayout, "Get all!!!", Snackbar.LENGTH_SHORT).show();
  }

  @Override
  protected void onFirstLoaded() {
    swipeRefreshLayout.post(() -> swipeRefreshLayout.setRefreshing(false));
  }

  @NonNull
  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    if (viewType == TYPE_LOAD_MORE) {
      final View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.load_more_item_layout, parent, false);
      return new LoadMoreVH(itemView);
    }
    if (viewType == TYPE_FIREBASE_MODEL_ITEM) {
      final View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.motel_room_item_layout, parent, false);
      final RecyclerOnClickListener recyclerOnClickListener =
          (view, position) -> onItemClick(view.getId(), ((MotelRoom) getItem(position)), position);
      return new MotelRoomVH(
          itemView,
          recyclerOnClickListener
      );
    }
    throw new IllegalStateException("Unknown view type " + viewType);
  }

  @Override
  public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
    Object item = getItem(position);

    if (holder instanceof LoadMoreVH) {
      ((LoadMoreVH) holder).bind();
    } else if (holder instanceof MotelRoomVH && item instanceof MotelRoom) {
      ((MotelRoomVH) holder).bind((MotelRoom) item);
    } else {
      throw new IllegalStateException("Unknown view holder " + holder);
    }
  }

  class LoadMoreVH extends RecyclerView.ViewHolder {
    private final ProgressBar progressBar;

    LoadMoreVH(@NonNull View itemView) {
      super(itemView);
      progressBar = itemView.findViewById(R.id.progressBar2);
    }

    void bind() {
      progressBar.setIndeterminate(true);
    }
  }
};*/
