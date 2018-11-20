package com.pkhh.projectcndd.screen.home;

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
import com.pkhh.projectcndd.utils.SharedPrefUtil;

import org.jetbrains.annotations.Contract;

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
import kotlin.collections.CollectionsKt;

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

  @BindView(R.id.root_motel_rooms_list_fragment) ViewGroup rootLayout;

  private Unbinder unbinder;

  @NonNull private List<MotelRoom> listRoomCreatedDes = emptyList();
  @NonNull private List<MotelRoom> listRoomCountViewDes = emptyList();
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

    final String selectedProvinceId = SharedPrefUtil.getInstance(requireContext()).getSelectedProvinceId();

    setupRecyclerViewAndAdapter(view);
    updateRecycler(listRoomCreatedDes, listRoomCountViewDes);
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

          listRoomCreatedDes = queryDocumentSnapshots != null ? FirebaseModel.querySnapshotToObjects(queryDocumentSnapshots, MotelRoom.class) : emptyList();
          updateRecycler(listRoomCreatedDes, listRoomCreatedDes);
        });


    registration2 = firestore.collection(ROOMS_NAME_COLLECION)
        .whereEqualTo("province", selectedProvinceRef)
        .whereEqualTo("is_active", true)
        .orderBy("count_view", Query.Direction.DESCENDING)
        .limit(LIMIT_COUNT_VIEW_DES)
        .addSnapshotListener((queryDocumentSnapshots, e) -> {
          if (e != null) return;

          listRoomCountViewDes = queryDocumentSnapshots != null ? FirebaseModel.querySnapshotToObjects(queryDocumentSnapshots, MotelRoom.class) : emptyList();
          updateRecycler(listRoomCreatedDes, listRoomCountViewDes);
        });
  }

  @NonNull
  @Contract("_ -> new")
  private RoomItem toRoomItem(MotelRoom room) {
    final int bookmarkIconState;
    final FirebaseUser currentUser = firebaseAuth.getCurrentUser();
    if (currentUser == null) bookmarkIconState = RoomItem.HIDE;
    else
      bookmarkIconState = room.getUserIdsSaved().contains(currentUser.getUid()) ? RoomItem.SHOW_SAVED : RoomItem.SHOW_NOT_SAVED;

    return new RoomItem(
        room.getId(),
        room.getTitle(),
        room.getPrice(),
        room.getAddress(),
        room.getDistrictName(),
        CollectionsKt.firstOrNull(room.getImages()),
        bookmarkIconState
    );
  }

  private void updateRecycler(List<MotelRoom> createdAtDes, List<MotelRoom> countViewDes) {
    List<HomeListItem> homeListItems = new ArrayList<>(1 + createdAtDes.size() + countViewDes.size());
    homeListItems.add(
        new BannerItem(
            Arrays.asList(
                new ImageAndDescriptionBanner("https://images.unsplash.com/photo-1504333638930-c8787321eee0?ixlib=rb-0.3.5&s=8fa53af55b1f12c07d2c3d4c1358c20a&w=1000&q=80", "Image 1"),
                new ImageAndDescriptionBanner("https://images.unsplash.com/photo-1483356256511-b48749959172?ixlib=rb-0.3.5&ixid=eyJhcHBfaWQiOjEyMDd9&s=fbb470799cbb69de82bc80822eee3255&w=1000&q=80", "Image 2"),
                new ImageAndDescriptionBanner("http://file.vforum.vn/hinh/2015/11/vforum.vn-hinh-anh-nen-ngan-ha-vu-tru-bao-la-10.jpg", "Image 3")
            )
        )
    );

    homeListItems.add(new HeaderItem("Mới nhất"));
    homeListItems.addAll(Stream.of(createdAtDes).map(this::toRoomItem).toList());
    homeListItems.add(new SeeAll(SeeAll.CREATED_AT_DESCENDING));


    homeListItems.add(new HeaderItem("Xem nhiều"));
    homeListItems.addAll(Stream.of(countViewDes).map(this::toRoomItem).toList());
    homeListItems.add(new SeeAll(SeeAll.COUNT_VIEW_DESCENDING));

    adapter.submitList(homeListItems);
  }

  @Override
  public void onPause() {
    super.onPause();

    registration1.remove();
    registration2.remove();
    firebaseAuth.removeAuthStateListener(this);
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

  private Void onAddToOrRemoveFromSavedRooms(String id) {
    firestore.runTransaction(transaction -> {
      final FirebaseUser currentUser = firebaseAuth.getCurrentUser();
      if (currentUser == null) {
        throw new IllegalStateException("Bạn phải login mới thưc hiện được chức năng này!");
      }

      final String uid = currentUser.getUid();
      final DocumentReference document = firestore.collection(ROOMS_NAME_COLLECION).document(id);

      List<?> userIdsSaved = (List) transaction.get(document).get("user_ids_saved");
      userIdsSaved = userIdsSaved == null ? emptyList() : userIdsSaved;

      if (userIdsSaved.contains(uid)) {

        transaction.update(document, "user_ids_saved", FieldValue.arrayRemove(uid));
        return "Xóa khỏi danh sach đã lưu thành công";

      } else {

        transaction.update(document, "user_ids_saved", FieldValue.arrayUnion(uid));
        return "Thêm vào danh sach đã lưu thành công";

      }
    }).continueWithTask(task -> {
      if (!task.isSuccessful()) throw Objects.requireNonNull(task.getException());
      Snackbar.make(rootLayout, task.getResult(), Snackbar.LENGTH_SHORT).show();
      return firestore.collection(ROOMS_NAME_COLLECION).document(id).get();
    }).addOnSuccessListener(requireActivity(), documentSnapshot -> {
      final MotelRoom room = FirebaseModel.documentSnapshotToObject(documentSnapshot, MotelRoom.class);
      listRoomCreatedDes = Stream.of(listRoomCreatedDes).map(r -> Objects.equals(r.getId(), id) ? room : r).toList();
      listRoomCountViewDes = Stream.of(listRoomCountViewDes).map(r -> Objects.equals(r.getId(), id) ? room : r).toList();
      updateRecycler(listRoomCreatedDes, listRoomCountViewDes);
    }).addOnFailureListener(Objects.requireNonNull(getActivity()), e -> Snackbar.make(rootLayout, "Error: " + e.getMessage(), Snackbar.LENGTH_SHORT).show());

    return null;
  }

  @Override
  public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
    updateRecycler(listRoomCreatedDes, listRoomCountViewDes);
  }
}