package com.pkhh.projectcndd.screen.posted;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.pkhh.projectcndd.R;
import com.pkhh.projectcndd.models.FirebaseModel;
import com.pkhh.projectcndd.models.MotelRoom;
import com.pkhh.projectcndd.utils.RecyclerOnClickListener;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

import static androidx.recyclerview.widget.ItemTouchHelper.RIGHT;
import static com.pkhh.projectcndd.utils.Constants.ROOMS_NAME_COLLECION;
import static com.pkhh.projectcndd.utils.Constants.USERS_NAME_COLLECION;
import static java.util.Objects.requireNonNull;


class PostedRoomVH extends RecyclerView.ViewHolder {
  public static final DecimalFormat PRICE_FORMAT = new DecimalFormat("###,###");

  @BindView(R.id.image_room) ImageView imageRoom;
  @BindView(R.id.text_room_title) TextView text_room_title;
  @BindView(R.id.text_room_price) TextView text_room_price;
  @BindView(R.id.text_room_address) TextView text_room_address;

  private final RecyclerOnClickListener onClickListener;

  public PostedRoomVH(@NonNull View itemView, RecyclerOnClickListener onClickListener) {
    super(itemView);
    this.onClickListener = onClickListener;
    ButterKnife.bind(this, itemView);
    itemView.setOnClickListener(v -> {
      final int adapterPosition = getAdapterPosition();
      if (adapterPosition != RecyclerView.NO_POSITION) {
        onClickListener.onClick(v, adapterPosition);
      }
    });
  }

  public void bind(MotelRoom item) {
    itemView.setTag(item.getId());
    List<String> imageUrls = item.getImages();
    if (imageUrls == null || imageUrls.isEmpty()) {
      imageRoom.setImageResource(R.drawable.ic_home_primary_dark_24dp);
    } else {
      Picasso.get()
          .load(imageUrls.get(0))
          .fit()
          .centerCrop()
          .placeholder(R.drawable.ic_home_primary_dark_24dp)
          .error(R.drawable.ic_home_primary_dark_24dp)
          .into(imageRoom);
    }

    text_room_title.setText(item.getTitle());
    text_room_address.setText(item.getAddress());
    text_room_price.setText(
        itemView.getContext()
            .getString(R.string.price_vnd_per_month, PRICE_FORMAT.format(item.getPrice()))
    );
  }
}

public class PostedRoomsActivity extends AppCompatActivity {
  @BindView(R.id.recycler) RecyclerView recyclerView;
  @BindView(R.id.spinner) Spinner spinner;
  private FirestoreRecyclerAdapter<MotelRoom, PostedRoomVH> adapter;

  private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
  private int prevSelectedPos;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_posted_room);
    ButterKnife.bind(this, this);

    requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    requireNonNull(getSupportActionBar()).setTitle(R.string.posted_rooms);

    spinner.setAdapter(
        new ArrayAdapter<>(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            Arrays.asList(getString(R.string.approved), getString(R.string.pending))
        )
    );
    spinner.setSelection(prevSelectedPos = 0);

    recyclerView.setHasFixedSize(true);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | RIGHT) {
      @Override
      public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
      }

      @Override
      public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        final String id = (String) viewHolder.itemView.getTag();
        firestore
            .document(ROOMS_NAME_COLLECION + "/" + id)
            .delete()
            .addOnSuccessListener(PostedRoomsActivity.this,
                __ -> Toast.makeText(PostedRoomsActivity.this, R.string.delete_room_successfully, Toast.LENGTH_SHORT).show())
            .addOnFailureListener(PostedRoomsActivity.this,
                e -> Toast.makeText(PostedRoomsActivity.this, getString(R.string.error, e.getMessage()), Toast.LENGTH_SHORT).show());
      }
    }).attachToRecyclerView(recyclerView);
    setupAdapter();

    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (prevSelectedPos == position) return;
        prevSelectedPos = position;
        setupAdapter();
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) { }
    });

  }

  private void setupAdapter() {
    if (adapter != null) {
      adapter.stopListening();
    }
    final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    final boolean approved = getString(R.string.approved).equals(spinner.getSelectedItem());

    final Query query = firestore.collection(ROOMS_NAME_COLLECION)
        .whereEqualTo("user", firestore.document(USERS_NAME_COLLECION + "/" + userId))
        .whereEqualTo("approve", approved)
        .orderBy("created_at", Query.Direction.DESCENDING);

    final FirestoreRecyclerOptions<MotelRoom> motelRoomFirestoreRecyclerOptions = new FirestoreRecyclerOptions.Builder<MotelRoom>()
        .setQuery(query, snapshot -> FirebaseModel.documentSnapshotToObject(snapshot, MotelRoom.class))
        .build();

    adapter = new FirestoreRecyclerAdapter<MotelRoom, PostedRoomVH>(motelRoomFirestoreRecyclerOptions) {
      @NonNull
      @Override
      public PostedRoomVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.posted_room_item_layout, parent, false);
        return new PostedRoomVH(itemView, this::onItemClick);
      }

      private void onItemClick(View __, int position) {
        Toast.makeText(PostedRoomsActivity.this, "TODO: edit room id = " + getItem(position).getId(), Toast.LENGTH_SHORT).show();
      }

      @Override
      protected void onBindViewHolder(@NonNull PostedRoomVH vh, int i, @NonNull MotelRoom motelRoom) {
        vh.bind(motelRoom);
      }
    };
    recyclerView.setAdapter(adapter);
    adapter.startListening();
    recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
      @Override
      public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        final int childAdapterPosition = parent.getChildAdapterPosition(view);
        if (childAdapterPosition != RecyclerView.NO_POSITION) {
          if (childAdapterPosition != parent.getAdapter().getItemCount() - 1) {
            outRect.bottom = 4;
          }
        }
      }
    });
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
