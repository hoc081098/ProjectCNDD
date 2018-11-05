package com.pkhh.projectcndd.ui.home;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.annimon.stream.Stream;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.pkhh.projectcndd.R;
import com.pkhh.projectcndd.models.FirebaseModel;
import com.pkhh.projectcndd.models.MotelRoom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.pkhh.projectcndd.utils.Constants.PROVINCES_NAME_COLLECION;
import static com.pkhh.projectcndd.utils.Constants.ROOMS_NAME_COLLECION;
import static java.util.Collections.emptyList;

public class MotelRoomsListFragment extends Fragment implements FirebaseAuth.AuthStateListener {
  public static final String TAG = MotelRoomsListFragment.class.getSimpleName();
  public static final int LIMIT_CREATED_DES = 20;
  public static final int LIMIT_COUNT_VIEW_DES = 20;

  private final String selectedProvinceId = "NtHjwobYdIi0YwTUHz05"; // TODO: user can change selected province

  private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
  private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

  private HomeAdapter adapter;

  @BindView(R.id.root_motel_rooms_list_fragment)
  ViewGroup rootLayout;

  private Unbinder unbinder;

  private List<HomeListItem> createdAtDes;
  private List<HomeListItem> countViewDes;
  private ListenerRegistration registration1;
  private ListenerRegistration registration2;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_motel_rooms_list, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    unbinder = ButterKnife.bind(this, view);

    createdAtDes = new ArrayList<>();
    createdAtDes.add(new HeaderItem("Mới nhất"));
    createdAtDes.add(new SeeAll(QueryDirection.CREATED_AT_DESCENDING));

    countViewDes = new ArrayList<>();
    countViewDes.add(new HeaderItem("Xem nhiều"));
    countViewDes.add(new SeeAll(QueryDirection.VIEW_COUNT_DESCENDING));

    setupRecyclerViewAndAdapter(view);
    updateRecycler(createdAtDes, countViewDes);
  }

  @Override
  public void onResume() {
    super.onResume();
    subscribe();
    firebaseAuth.addAuthStateListener(this);
  }

  private void subscribe() {
    final DocumentReference selectedProvinceRef = firestore.document(PROVINCES_NAME_COLLECION + "/" + selectedProvinceId);

    registration1 = firestore.collection(ROOMS_NAME_COLLECION)
        .whereEqualTo("province", selectedProvinceRef)
        .whereEqualTo("is_active", true)
        .orderBy("created_at", Query.Direction.DESCENDING)
        .limit(LIMIT_CREATED_DES)
        .addSnapshotListener((queryDocumentSnapshots, e) -> {
          if (e != null) return;

          final List<RoomItem> newRooms = queryDocumentSnapshots != null ? Stream.of(FirebaseModel.querySnapshotToObjects(queryDocumentSnapshots, MotelRoom.class))
              .map(RoomItem::new)
              .toList() : emptyList();

          createdAtDes = new ArrayList<>(newRooms.size() + 2);
          createdAtDes.add(new HeaderItem("Mới nhất"));
          createdAtDes.addAll(newRooms);
          createdAtDes.add(new SeeAll(QueryDirection.CREATED_AT_DESCENDING));

          updateRecycler(createdAtDes, countViewDes);
        });


    registration2 = firestore.collection(ROOMS_NAME_COLLECION)
        .whereEqualTo("province", selectedProvinceRef)
        .whereEqualTo("is_active", true)
        .orderBy("count_view", Query.Direction.DESCENDING)
        .limit(LIMIT_COUNT_VIEW_DES)
        .addSnapshotListener((queryDocumentSnapshots, e) -> {
          if (e != null) return;

          final List<RoomItem> newRooms = queryDocumentSnapshots != null ? Stream.of(FirebaseModel.querySnapshotToObjects(queryDocumentSnapshots, MotelRoom.class))
              .map(RoomItem::new)
              .toList() : emptyList();

          countViewDes = new ArrayList<>(newRooms.size() + 2);
          countViewDes.add(new HeaderItem("Xem nhiều"));
          countViewDes.addAll(newRooms);
          countViewDes.add(new SeeAll(QueryDirection.CREATED_AT_DESCENDING));

          updateRecycler(createdAtDes, countViewDes);
        });
  }

  private void updateRecycler(List<HomeListItem> createdAtDes, List<HomeListItem> countViewDes) {
    final List<HomeListItem> homeListItems = new ArrayList<>(1 + createdAtDes.size() + countViewDes.size());
    homeListItems.add(
        new BannerItem(
            Arrays.asList(
                new ImageAndDescriptionBanner("https://images.unsplash.com/photo-1504333638930-c8787321eee0?ixlib=rb-0.3.5&s=8fa53af55b1f12c07d2c3d4c1358c20a&w=1000&q=80", "Image 1"),
                new ImageAndDescriptionBanner("https://images.unsplash.com/photo-1483356256511-b48749959172?ixlib=rb-0.3.5&ixid=eyJhcHBfaWQiOjEyMDd9&s=fbb470799cbb69de82bc80822eee3255&w=1000&q=80", "Image 2"),
                new ImageAndDescriptionBanner("http://file.vforum.vn/hinh/2015/11/vforum.vn-hinh-anh-nen-ngan-ha-vu-tru-bao-la-10.jpg", "Image 3")
            )
        )
    );
    homeListItems.addAll(createdAtDes);
    homeListItems.addAll(countViewDes);
    adapter.submitList(homeListItems);
  }

  @Override
  public void onPause() {
    super.onPause();

    firebaseAuth.removeAuthStateListener(this);
    registration1.remove();
    registration2.remove();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    unbinder.unbind();
  }

  private void setupRecyclerViewAndAdapter(@NonNull View view) {
    adapter = new HomeAdapter(this::onAddToOrRemoveFromSavedRooms);

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
  }

  private Void onAddToOrRemoveFromSavedRooms(MotelRoom motelRoom) {
    firestore.runTransaction(transaction -> {
      final FirebaseUser currentUser = firebaseAuth.getCurrentUser();
      if (currentUser == null) {
        throw new IllegalStateException("Bạn phải login mới thưc hiện được chức năng này!");
      }

      final String uid = currentUser.getUid();
      final DocumentReference document = firestore.collection(ROOMS_NAME_COLLECION).document(motelRoom.getId());

      List<?> userIdsSaved = (List) transaction.get(document).get("user_ids_saved");
      userIdsSaved = userIdsSaved == null ? emptyList() : userIdsSaved;

      if (userIdsSaved.contains(uid)) {

        transaction.update(document, "user_ids_saved", FieldValue.arrayRemove(uid));
        return "Xóa khỏi danh sach đã lưu thành công";

      } else {

        transaction.update(document, "user_ids_saved", FieldValue.arrayUnion(uid));
        return "Thêm vào danh sach đã lưu thành công";

      }
    }).addOnSuccessListener(Objects.requireNonNull(getActivity()), message -> Snackbar.make(rootLayout, message, Snackbar.LENGTH_SHORT).show())
        .addOnFailureListener(Objects.requireNonNull(getActivity()), e -> Snackbar.make(rootLayout, "Error: " + e.getMessage(), Snackbar.LENGTH_SHORT).show());

    return null;
  }

  @Override
  public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
    if (firebaseAuth.getCurrentUser() == null) {
      adapter.notifyDataSetChanged();
    }
  }
}