package com.pkhh.projectcndd.ui.saved;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.pkhh.projectcndd.R;
import com.pkhh.projectcndd.models.FirebaseModel;
import com.pkhh.projectcndd.models.MotelRoom;
import com.pkhh.projectcndd.ui.detail.MotelRoomDetailActivity;
import com.pkhh.projectcndd.utils.Constants;
import com.pkhh.projectcndd.utils.RecyclerOnClickListener;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.pkhh.projectcndd.utils.Constants.MOTEL_ROOM_ID;

class SavedRoomAdapter extends FirestoreRecyclerAdapter<MotelRoom, SavedRoomAdapter.ViewHolder> {
  public static final DecimalFormat PRICE_FORMAT = new DecimalFormat("###,###");

  private final RecyclerOnClickListener onClickListener;

  SavedRoomAdapter(@NonNull FirestoreRecyclerOptions<MotelRoom> options, RecyclerOnClickListener onClickListener) {
    super(options);
    this.onClickListener = onClickListener;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.saved_room_item_layout, parent, false);
    return new ViewHolder(itemView);
  }

  @Override
  protected void onBindViewHolder(@NonNull ViewHolder viewHolder, int i, @NonNull MotelRoom motelRoom) {
    viewHolder.bind(motelRoom);
  }

  class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    @BindView(R.id.image_saved_room)
    ImageView image;

    @BindView(R.id.image_saved_room_bookmark)
    AppCompatImageView imageRemoveBookmark;

    @BindView(R.id.text_saved_room_address)
    TextView textAddress;

    @BindView(R.id.text_saved_room_price)
    TextView textPrice;

    @BindView(R.id.text_saved_room_title)
    TextView textTitle;

    ViewHolder(@NonNull View itemView) {
      super(itemView);

      ButterKnife.bind(this, itemView);

      itemView.setOnClickListener(this);
      imageRemoveBookmark.setOnClickListener(this);
    }

    void bind(MotelRoom item) {
      List<String> imageUrls = item.getImages();
      if (imageUrls == null || imageUrls.isEmpty()) {
        image.setImageResource(R.drawable.ic_home_primary_dark_24dp);
      } else {
        Picasso.get()
            .load(imageUrls.get(0))
            .fit()
            .centerCrop()
            .placeholder(R.drawable.ic_home_primary_dark_24dp)
            .error(R.drawable.ic_home_primary_dark_24dp)
            .into(image);
      }

      textTitle.setText(item.getTitle());
      textAddress.setText(item.getAddress());
      textPrice.setText(PRICE_FORMAT.format(item.getPrice()) + "đ/tháng");
    }

    @Override
    public void onClick(View v) {
      final int adapterPosition = getAdapterPosition();
      if (adapterPosition != RecyclerView.NO_POSITION) {
        onClickListener.onClick(v, adapterPosition);
      }
    }
  }
}

public final class SavedRoomsActivity extends AppCompatActivity implements RecyclerOnClickListener {
  private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
  private final FirebaseAuth auth = FirebaseAuth.getInstance();

  private SavedRoomAdapter adapter;

  @BindView(R.id.recycler_saved_room)
  RecyclerView recyclerView;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_saved_room);

    ButterKnife.bind(this, this);
    Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

    setupRecyclerView();
  }

  private void setupRecyclerView() {
    recyclerView.setHasFixedSize(true);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

    final Query query = firestore
        .collection(Constants.MOTEL_ROOM_NAME_COLLECION)
        .whereArrayContains("user_ids_saved", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());

    final FirestoreRecyclerOptions<MotelRoom> motelRoomFirestoreRecyclerOptions = new FirestoreRecyclerOptions.Builder<MotelRoom>()
        .setQuery(query, snapshot -> FirebaseModel.documentSnapshotToObject(snapshot, MotelRoom.class))
        .build();

    adapter = new SavedRoomAdapter(motelRoomFirestoreRecyclerOptions, this);
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
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onClick(@NonNull View view, int position) {
    final MotelRoom item = adapter.getItem(position);

    if (view.getId() == R.id.image_saved_room_bookmark) {
      removeSaved(item);
    } else {
      final Intent intent = new Intent(this, MotelRoomDetailActivity.class);
      intent.putExtra(MOTEL_ROOM_ID, item.getId());
      startActivity(intent);
    }
  }

  private void removeSaved(MotelRoom item) {
    final FirebaseUser currentUser = auth.getCurrentUser();
    if (currentUser == null) {
      Toast.makeText(this, "Something is wrong?", Toast.LENGTH_SHORT).show();
      return;
    }

    firestore.document(Constants.MOTEL_ROOM_NAME_COLLECION + "/" + item.getId())
        .update("user_ids_saved", FieldValue.arrayRemove(currentUser.getUid()))
        .addOnSuccessListener(this, aVoid -> {
          Snackbar.make(recyclerView.getRootView(), "Done", Snackbar.LENGTH_SHORT).show();
        })
        .addOnFailureListener(this, e -> {
          Snackbar.make(recyclerView.getRootView(), e.getMessage(), Snackbar.LENGTH_SHORT).show();
        });
  }
}
