package com.pkhh.projectcndd.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.pkhh.projectcndd.R;
import com.pkhh.projectcndd.models.FirebaseModel;
import com.pkhh.projectcndd.models.MotelRoom;
import com.pkhh.projectcndd.ui.detail.MotelRoomDetailActivity;
import com.pkhh.projectcndd.utils.RecyclerOnClickListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import static com.pkhh.projectcndd.utils.Constants.MOTEL_ROOM_ID;
import static com.pkhh.projectcndd.utils.Constants.MOTEL_ROOM_NAME_COLLECION;
import static java.util.Objects.requireNonNull;

public class MotelRoomsListFragment extends Fragment {
  public static final String TAG = MotelRoomsListFragment.class.getSimpleName();
  private static final int PAGE_SIZE = 30;

  private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
  private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

  private ViewGroup rootLayout;
  private MyFirebaseLoadMoreAdapter<MotelRoom> adapter;
  private SwipeRefreshLayout swipeRefreshLayout;

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
    swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
    swipeRefreshLayout.setOnRefreshListener(() -> {
      if (adapter != null) {
        adapter.refresh();
      }
    });
    swipeRefreshLayout.post(() -> swipeRefreshLayout.setRefreshing(true));

    FirebaseFirestore.setLoggingEnabled(true);
  }

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

  }


  private void setupRecyclerViewAndAdapter(@NonNull View view) {
    RecyclerView recyclerView = view.findViewById(R.id.recycler);
    recyclerView.setHasFixedSize(true);
    recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

    Query query = firestore.collection(MOTEL_ROOM_NAME_COLLECION)
        .orderBy("created_at", Query.Direction.DESCENDING);

    adapter = new MyFirebaseLoadMoreAdapter<MotelRoom>(query, PAGE_SIZE, recyclerView, MotelRoom.class) {
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
    };

    recyclerView.setAdapter(adapter);
  }
}
