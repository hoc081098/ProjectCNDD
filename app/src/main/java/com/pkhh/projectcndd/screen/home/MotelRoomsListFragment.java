package com.pkhh.projectcndd.screen.home;

import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

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
import com.pkhh.projectcndd.models.Province;
import com.pkhh.projectcndd.utils.SharedPrefUtil;

import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import kotlin.collections.CollectionsKt;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;
import static com.annimon.stream.Collectors.toList;
import static com.annimon.stream.Collectors.toMap;
import static com.annimon.stream.Stream.of;
import static com.pkhh.projectcndd.models.FirebaseModel.querySnapshotToObjects;
import static com.pkhh.projectcndd.screen.home.RoomItem.HIDE;
import static com.pkhh.projectcndd.screen.home.RoomItem.SHOW_NOT_SAVED;
import static com.pkhh.projectcndd.screen.home.RoomItem.SHOW_SAVED;
import static com.pkhh.projectcndd.utils.Constants.PROVINCES_NAME_COLLECION;
import static com.pkhh.projectcndd.utils.Constants.ROOMS_NAME_COLLECION;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;

public class MotelRoomsListFragment extends Fragment implements FirebaseAuth.AuthStateListener {
  public static final String TAG = MotelRoomsListFragment.class.getSimpleName();
  public static final int LIMIT_CREATED_DES = 14;
  public static final int LIMIT_COUNT_VIEW_DES = 14;

  private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
  private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

  private HomeAdapter adapter;
  private String selectedProvinceId;
  private String selectedProvinceName;

  @BindView(R.id.root_motel_rooms_list_fragment) ViewGroup rootLayout;
  private Unbinder unbinder;
  @Nullable private AlertDialog changeProvinceDialog;


  @NonNull private List<MotelRoom> listRoomCreatedDes = emptyList();
  @NonNull private List<MotelRoom> listRoomCountViewDes = emptyList();
  @Nullable private ListenerRegistration registration1;
  @Nullable private ListenerRegistration registration2;

  @NonNull private List<Province> provinces = emptyList();
  @NonNull private Map<String, Province> provinceMap = emptyMap();
  @NonNull private List<CharSequence> provinceNames = emptyList();
  @NonNull private List<String> provinceIds = emptyList();

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_motel_rooms_list, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    unbinder = ButterKnife.bind(this, view);

    selectedProvinceId = SharedPrefUtil.getInstance(requireContext()).getSelectedProvinceId(getString(R.string.da_nang_id));
    selectedProvinceName = SharedPrefUtil.getInstance(requireContext()).getSelectedProvinceName(getString(R.string.da_nang_name));

    setupRecyclerViewAndAdapter(view);
    updateRecycler(listRoomCreatedDes, listRoomCountViewDes);

    firestore.collection(PROVINCES_NAME_COLLECION)
        .get()
        .addOnSuccessListener(requireActivity(), queryDocumentSnapshots -> {
          provinces = querySnapshotToObjects(queryDocumentSnapshots, Province.class);
          provinceMap = of(provinces).collect(toMap(FirebaseModel::getId, i -> i));
          provinceNames = of(provinces).map(Province::getName).collect(toList());
          provinceIds = of(provinces).map(FirebaseModel::getId).toList();
        });
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

          listRoomCreatedDes = queryDocumentSnapshots != null ? querySnapshotToObjects(queryDocumentSnapshots, MotelRoom.class) : emptyList();
          updateRecycler(listRoomCreatedDes, listRoomCreatedDes);
        });


    registration2 = firestore.collection(ROOMS_NAME_COLLECION)
        .whereEqualTo("province", selectedProvinceRef)
        .whereEqualTo("is_active", true)
        .orderBy("count_view", Query.Direction.DESCENDING)
        .limit(LIMIT_COUNT_VIEW_DES)
        .addSnapshotListener((queryDocumentSnapshots, e) -> {
          if (e != null) return;

          listRoomCountViewDes = queryDocumentSnapshots != null ? querySnapshotToObjects(queryDocumentSnapshots, MotelRoom.class) : emptyList();
          updateRecycler(listRoomCreatedDes, listRoomCountViewDes);
        });
  }

  @NonNull
  @Contract("_ -> new")
  private RoomItem toRoomItem(MotelRoom room) {
    final int bookmarkIconState;
    final FirebaseUser currentUser = firebaseAuth.getCurrentUser();
    if (currentUser == null) {
      bookmarkIconState = HIDE;
    } else {
      bookmarkIconState = room.getUserIdsSaved().contains(currentUser.getUid()) ? SHOW_SAVED : SHOW_NOT_SAVED;
    }

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
    List<HomeListItem> homeListItems = new ArrayList<>(5 + createdAtDes.size() + countViewDes.size());

    homeListItems.add(
        new BannerItem(
            Arrays.asList(
                new ImageAndDescriptionBanner("https://images.unsplash.com/photo-1504333638930-c8787321eee0?ixlib=rb-0.3.5&s=8fa53af55b1f12c07d2c3d4c1358c20a&w=1000&q=80", "Image 1"),
                new ImageAndDescriptionBanner("https://images.unsplash.com/photo-1483356256511-b48749959172?ixlib=rb-0.3.5&ixid=eyJhcHBfaWQiOjEyMDd9&s=fbb470799cbb69de82bc80822eee3255&w=1000&q=80", "Image 2"),
                new ImageAndDescriptionBanner("http://file.vforum.vn/hinh/2015/11/vforum.vn-hinh-anh-nen-ngan-ha-vu-tru-bao-la-10.jpg", "Image 3")
            ),
            selectedProvinceName
        )
    );

    homeListItems.add(new HeaderItem("Mới nhất"));
    homeListItems.addAll(of(createdAtDes).map(this::toRoomItem).toList());
    homeListItems.add(new SeeAll(SeeAll.CREATED_AT_DESCENDING));

    homeListItems.add(new HeaderItem("Xem nhiều"));
    homeListItems.addAll(of(countViewDes).map(this::toRoomItem).toList());
    homeListItems.add(new SeeAll(SeeAll.COUNT_VIEW_DESCENDING));

    Log.d("@@@", "submit List " + homeListItems);
    adapter.submitList(homeListItems);
  }

  @Override
  public void onPause() {
    super.onPause();

    if (registration1 != null) {
      registration1.remove();
    }
    if (registration2 != null) {
      registration2.remove();
    }
    firebaseAuth.removeAuthStateListener(this);

    if (changeProvinceDialog != null && changeProvinceDialog.isShowing()) {
      changeProvinceDialog.dismiss();
    }
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    unbinder.unbind();
  }

  private void setupRecyclerViewAndAdapter(@NonNull View view) {
    adapter = new HomeAdapter(this::onAddToOrRemoveFromSavedRooms, this::onChangeLocationClick);

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
    recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
      @Override
      public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        final int space = 4;
        final int position = parent.getChildAdapterPosition(view);

        if (position != NO_POSITION &&
            adapter.getItemViewType(position) == R.layout.home_room_item_layout) {
          outRect.left = space;
          outRect.right = space;
          outRect.bottom = space;
          outRect.top = space;
        }
      }
    });
  }

  private Void onChangeLocationClick() {
    final ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(requireContext(),
        android.R.layout.simple_list_item_1, provinceNames);

    changeProvinceDialog = new AlertDialog.Builder(requireContext())
        .setTitle("Thay đổi khu vực")
        .setSingleChoiceItems(adapter, provinceIds.indexOf(selectedProvinceId), (dialog, position) -> {
          if (registration1 != null) {
            registration1.remove();
          }
          if (registration2 != null) {
            registration2.remove();
          }
          dialog.dismiss();

          selectedProvinceId = provinceIds.get(position);
          selectedProvinceName = requireNonNull(provinceMap.get(selectedProvinceId)).getName();
          updateRecycler(listRoomCreatedDes = emptyList(), listRoomCountViewDes = emptyList());
          subscribe();

          final SharedPrefUtil sharedPrefUtil = SharedPrefUtil.getInstance(requireContext());
          sharedPrefUtil.saveSelectedProvinceId(selectedProvinceId);
          sharedPrefUtil.saveSelectedProvinceName(selectedProvinceId);
        })
        .setNegativeButton("Cancel", (dialog, __) -> dialog.dismiss())
        .show();

    return null;
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
      if (!task.isSuccessful()) throw requireNonNull(task.getException());

      Snackbar.make(rootLayout, requireNonNull(task.getResult()), Snackbar.LENGTH_SHORT).show();
      return firestore.collection(ROOMS_NAME_COLLECION).document(id).get();

    }).addOnSuccessListener(requireActivity(), documentSnapshot -> {

      final MotelRoom room = FirebaseModel.documentSnapshotToObject(documentSnapshot, MotelRoom.class);

      listRoomCreatedDes = of(listRoomCreatedDes).map(r -> Objects.equals(r.getId(), id) ? room : r).toList();
      listRoomCountViewDes = of(listRoomCountViewDes).map(r -> Objects.equals(r.getId(), id) ? room : r).toList();

      updateRecycler(listRoomCreatedDes, listRoomCountViewDes);

    }).addOnFailureListener(requireNonNull(getActivity()), e -> Snackbar.make(rootLayout, "Error: " + e.getMessage(), Snackbar.LENGTH_SHORT).show());

    return null;
  }

  @Override
  public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
    updateRecycler(listRoomCreatedDes, listRoomCountViewDes);
  }
}