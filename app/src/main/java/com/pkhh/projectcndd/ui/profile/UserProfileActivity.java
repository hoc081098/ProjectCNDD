package com.pkhh.projectcndd.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.pkhh.projectcndd.R;
import com.pkhh.projectcndd.models.FirebaseModel;
import com.pkhh.projectcndd.models.MotelRoom;
import com.pkhh.projectcndd.models.User;
import com.pkhh.projectcndd.ui.detail.MotelRoomDetailActivity;
import com.pkhh.projectcndd.ui.saved.SavedViewHolder;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
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
import static com.pkhh.projectcndd.utils.Constants.ROOMS_NAME_COLLECION;
import static com.pkhh.projectcndd.utils.Constants.USERS_NAME_COLLECION;

public class UserProfileActivity extends AppCompatActivity {

    public static final int PAGE_SIZE = 15;

    @BindView(R.id.root_user_profile)
    CoordinatorLayout root;
    @BindView(R.id.collapsing_toolbar_layout)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.appbar)
    AppBarLayout appBarLayout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.image_avatar)
    ImageView imageAvatar;
    @BindView(R.id.recycler_post_profile)
    RecyclerView recyclerView;
    @BindView(R.id.app_bar_image)
    ImageView appBarImage;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.text_email)
    TextView textEmail;
    @BindView(R.id.text_name)
    TextView textName;
    @BindView(R.id.text_address)
    TextView textAddress;

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private FirestorePagingAdapter<MotelRoom, SavedViewHolder> adapter;
    private String userName = " ";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        ButterKnife.bind(this, this);

        final String userId = getIntent().getStringExtra(MotelRoomDetailActivity.EXTRA_USER_ID);
        userName = getIntent().getStringExtra(MotelRoomDetailActivity.EXTRA_USER_FULL_NAME);
        textName.setText(userName);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            private boolean isShow = true;
            private int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbarLayout.setTitle(userName);
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbarLayout.setTitle(" ");//carefull there should a space between double quote otherwise it wont work
                    isShow = false;
                }
            }
        });

        firestore.document(USERS_NAME_COLLECION + "/" + userId)
                .addSnapshotListener(this, (snapshot, e) -> {
                    if (e != null) {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshot != null) {
                        final User user = FirebaseModel.documentSnapshotToObject(snapshot, User.class);
                        userName = user.getFullName();

                        String avatar = user.getAvatar();
                        if (!avatar.isEmpty())
                            Picasso.get()
                                    .load(avatar)
                                    .fit()
                                    .centerCrop()
                                    .noFade()
                                    .into(imageAvatar);

                        textEmail.setText(user.getEmail());
                        textName.setText(user.getFullName());
                        textAddress.setText(user.getAddress());
                    }
                });

        final Query query = firestore.collection(ROOMS_NAME_COLLECION)
                .whereEqualTo("user", firestore.document(USERS_NAME_COLLECION + "/" + userId))
                .orderBy("created_at", Query.Direction.DESCENDING);

        final PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setInitialLoadSizeHint(PAGE_SIZE)
                .setPageSize(PAGE_SIZE)
                .setPrefetchDistance(5)
                .build();

        final FirestorePagingOptions<MotelRoom> options = new FirestorePagingOptions.Builder<MotelRoom>()
                .setQuery(query, config, snapshot -> FirebaseModel.documentSnapshotToObject(snapshot, MotelRoom.class))
                .build();

        adapter = new FirestorePagingAdapter<MotelRoom, SavedViewHolder>(options) {

            @NonNull
            @Override
            public SavedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.saved_room_item_layout, parent, false);
                return new SavedViewHolder(itemView, this::onClick);
            }

            private void onClick(View view, int position) {
                final DocumentSnapshot snapshot = getItem(position);
                if (snapshot != null) {
                    final Intent intent = new Intent(UserProfileActivity.this, MotelRoomDetailActivity.class);
                    intent.putExtra(MOTEL_ROOM_ID, snapshot.getId());
                    startActivity(intent);
                }
            }

            @Override
            protected void onBindViewHolder(@NonNull SavedViewHolder savedViewHolder, int i, @NonNull MotelRoom motelRoom) {
                savedViewHolder.bind(motelRoom);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adapter.stopListening();
    }
}
