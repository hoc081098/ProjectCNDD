package com.pkhh.projectcndd.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.pkhh.projectcndd.R;
import com.pkhh.projectcndd.models.FirebaseModel;
import com.pkhh.projectcndd.models.MotelRoom;
import com.pkhh.projectcndd.ui.detail.MotelRoomDetailActivity;
import com.pkhh.projectcndd.utils.RecyclerOnClickListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import static com.pkhh.projectcndd.utils.Constants.MOTEL_ROOM_ID;
import static com.pkhh.projectcndd.utils.Constants.MOTEL_ROOM_NAME_COLLECION;
import static com.pkhh.projectcndd.utils.Constants.SAVED_ROOMS_NAME_COLLECTION;
import static com.pkhh.projectcndd.utils.Constants.USER_NAME_COLLECION;

final class RoomItem extends FirebaseModel {
    @NonNull
    final MotelRoom motelRoom;
    final boolean isSaved;
    final boolean isLogined;

    RoomItem(@NonNull MotelRoom motelRoom, boolean isSaved, boolean isLogined) {
        this.motelRoom = motelRoom;
        this.isSaved = isSaved;
        this.isLogined = isLogined;
        this.id = motelRoom.getId();
    }

    @NonNull
    RoomItem loginAnd(boolean isSaved) {
        return new RoomItem(motelRoom, isSaved, true);
    }

    @NonNull
    RoomItem notSave() {
        return new RoomItem(motelRoom, false, isLogined);
    }

    @NonNull
    RoomItem save() {
        return new RoomItem(motelRoom, true, isLogined);
    }

    @NonNull
    RoomItem notLoginAndNotSave() {
        return new RoomItem(motelRoom, false, false);
    }
}

public class MotelRoomsListFragment extends Fragment implements FirebaseAuth.AuthStateListener {
    public static final String TAG = MotelRoomsListFragment.class.getSimpleName();
    private static final int PAGE_SIZE = 30;

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    private ViewGroup rootLayout;
    @Nullable
    private MyFirebaseLoadMoreAdapter<RoomItem> adapter;
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

    @Override
    public void onResume() {
        super.onResume();
        Log.d("@@@", "onResume");
        firebaseAuth.addAuthStateListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("@@@", "onPause");
        firebaseAuth.removeAuthStateListener(this);
    }

    private void onItemClick(int viewId, @NonNull RoomItem roomItem, int position) {
        if (viewId == R.id.image_share) {
            Toast.makeText(requireContext(), "Share clicked", Toast.LENGTH_SHORT).show();
            onShareClicked(roomItem.getId());
            return;
        }

        if (viewId == R.id.image_save) {
            onSaveClicked(roomItem, position);
            return;
        }

        Intent intent = new Intent(requireContext(), MotelRoomDetailActivity.class);
        intent.putExtra(MOTEL_ROOM_ID, roomItem.getId());
        startActivity(intent);
    }

    private void onSaveClicked(RoomItem roomItem, int position) {
        final FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(requireContext(), "Bạn phải login!", Toast.LENGTH_SHORT).show();
            return;
        }

        final DocumentReference document = firestore.collection(USER_NAME_COLLECION)
                .document(currentUser.getUid())
                .collection(SAVED_ROOMS_NAME_COLLECTION)
                .document(roomItem.getId());

        if (roomItem.isSaved) {
            document.delete()
                    .addOnSuccessListener(aVoid -> {
                        if (adapter != null) {
                            adapter.getList().set(position, roomItem.notSave());
                            adapter.notifyItemChanged(position);
                            Snackbar.make(rootLayout, "Bỏ lưu thành công", Snackbar.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.d(TAG, e.getMessage(), e);
                        Snackbar.make(rootLayout, "Bỏ lưu thất bại!", Snackbar.LENGTH_SHORT).show();
                    });
        } else {
            Map<String, Object> map = new HashMap<>();
            map.put("created_at", FieldValue.serverTimestamp());

            document.set(map)
                    .addOnSuccessListener(aVoid -> {
                        if (adapter != null) {
                            adapter.getList().set(position, roomItem.save());
                            adapter.notifyItemChanged(position);
                            Log.d("@@@", "save suc " + adapter.getItem(position));
                            Snackbar.make(rootLayout, "Lưu thành công", Snackbar.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.d(TAG, e.getMessage(), e);
                        Snackbar.make(rootLayout, "Lưu thất bại!", Snackbar.LENGTH_SHORT).show();
                    });
        }

    }

    private void onShareClicked(String modelId) {

    }


    private void setupRecyclerViewAndAdapter(@NonNull View view) {
        RecyclerView recyclerView = view.findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        Query query = firestore.collection(MOTEL_ROOM_NAME_COLLECION)
                .orderBy("created_at", Query.Direction.DESCENDING);

        final FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            firestore.collection(USER_NAME_COLLECION + "/" + currentUser.getUid() + "/" + SAVED_ROOMS_NAME_COLLECTION)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        final Set<String> savedIds = Stream.of(queryDocumentSnapshots.getDocuments())
                                .map(DocumentSnapshot::getId)
                                .collect(Collectors.toUnmodifiableSet());


                        adapter = new MyFirebaseLoadMoreAdapter<RoomItem>(query, PAGE_SIZE, recyclerView,
                                RoomItem.class,
                                snapshot -> new RoomItem(
                                        FirebaseModel.documentSnapshotToObject(snapshot, MotelRoom.class),
                                        savedIds.contains(snapshot.getId()),
                                        firebaseAuth.getCurrentUser() != null)) {
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
                                            (view, position) -> onItemClick(view.getId(), ((RoomItem) getItem(position)), position);
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
                                } else if (holder instanceof MotelRoomVH && item instanceof RoomItem) {
                                    ((MotelRoomVH) holder).bind((RoomItem) item);
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


                    })
                    .addOnFailureListener(e -> {

                    });
        }
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        if (adapter == null) return;
        final List<Object> list = adapter.getList();

        final FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            if (adapter == null) return;
            adapter.setList(
                    Stream.of(list)
                            .map(e -> {
                                if (e instanceof RoomItem) {
                                    return ((RoomItem) e).notLoginAndNotSave();
                                }
                                return e;
                            })
                            .toList()
            );
            return;
        }

        firestore.collection(USER_NAME_COLLECION + "/" + currentUser.getUid()
                + "/" + SAVED_ROOMS_NAME_COLLECTION)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    final Set<String> savedIds = Stream.of(queryDocumentSnapshots.getDocuments())
                            .map(DocumentSnapshot::getId)
                            .collect(Collectors.toUnmodifiableSet());

                    if (adapter == null) return;
                    adapter.setList(
                            Stream.of(list)
                                    .map(e -> {
                                        if (e instanceof RoomItem) {
                                            final RoomItem item = (RoomItem) e;
                                            return item.loginAnd(savedIds.contains(item.getId()));
                                        }
                                        return e;
                                    })
                                    .toList()
                    );
                })
                .addOnFailureListener(e -> {

                });

    }
}
