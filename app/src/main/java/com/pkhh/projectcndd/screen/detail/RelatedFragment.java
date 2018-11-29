package com.pkhh.projectcndd.screen.detail;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.annimon.stream.function.Consumer;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.pkhh.projectcndd.R;
import com.pkhh.projectcndd.models.FirebaseModel;
import com.pkhh.projectcndd.models.MotelRoom;
import com.pkhh.projectcndd.utils.Constants;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;
import static com.pkhh.projectcndd.screen.saved.SavedViewHolder.PRICE_FORMAT;
import static com.pkhh.projectcndd.utils.Constants.EXTRA_MOTEL_ROOM_ID;

class RelatedRoomItemVH extends RecyclerView.ViewHolder implements View.OnClickListener {

  @BindView(R.id.text_saved_room_address) TextView textAddress;
  @BindView(R.id.text_saved_room_price) TextView textPrice;
  @BindView(R.id.text_saved_room_title) TextView textTitle;
  @BindView(R.id.image_saved_room) ImageView image;
  @BindView(R.id.text_home_room_district) TextView textDistrict;
  @BindView(R.id.image_saved_room_bookmark) ImageView imageSave;

  @NonNull private final Consumer<Integer> onClickListener;

  RelatedRoomItemVH(@NonNull View itemView, @NonNull Consumer<Integer> onClickListener) {
    super(itemView);
    this.onClickListener = onClickListener;
    ButterKnife.bind(this, itemView);

    itemView.setOnClickListener(this);
    imageSave.setVisibility(View.INVISIBLE);
  }

  @Override
  public void onClick(View v) {
    final int adapterPosition = getAdapterPosition();
    if (adapterPosition != NO_POSITION) {
      onClickListener.accept(adapterPosition);
    }
  }

  public void bind(MotelRoom room) {
    textDistrict.setText(room.getDistrictName());
    textAddress.setText(room.getAddress());
    textPrice.setText(itemView.getContext().getString(R.string.price_vnd_per_month, PRICE_FORMAT.format(room.getPrice())));
    textTitle.setText(room.getTitle());


    if (room.getImages() == null || room.getImages().isEmpty()) {
      image.setImageResource(R.drawable.ic_home_primary_dark_24dp);
    } else {
      Picasso.get()
          .load(room.getImages().get(0))
          .fit()
          .centerCrop()
          .placeholder(R.drawable.ic_home_primary_dark_24dp)
          .error(R.drawable.ic_home_primary_dark_24dp)
          .into(image);
    }
  }
}

public class RelatedFragment extends Fragment {
  static final long LIMIT = 20;
  static final long DELTA_PRICE = 500_000L;

  private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

  private Unbinder unbinder;
  @Nullable private FirestoreRecyclerAdapter<MotelRoom, RelatedRoomItemVH> adapter;

  @BindView(R.id.recycler_related) RecyclerView recyclerView;
  @BindView(R.id.progressbar) ProgressBar progressbar;

  public static RelatedFragment newInstance(String id) {
    final RelatedFragment fragment = new RelatedFragment();
    final Bundle args = new Bundle();
    args.putString(EXTRA_MOTEL_ROOM_ID, id);
    fragment.setArguments(args);
    return fragment;
  }


  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_related, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    unbinder = ButterKnife.bind(this, view);

    String roomId = Objects.requireNonNull(getArguments()).getString(EXTRA_MOTEL_ROOM_ID);

    firestore
        .document(Constants.ROOMS_NAME_COLLECION + "/" + roomId)
        .get()
        .continueWith(task -> FirebaseModel.documentSnapshotToObject(Objects.requireNonNull(task.getResult()), MotelRoom.class))
        .addOnSuccessListener(requireActivity(), this::setupRecycler);
  }

  private void setupRecycler(MotelRoom room) {
    Query query = firestore.collection(Constants.ROOMS_NAME_COLLECION)
        .whereEqualTo("approve", true)
        .whereEqualTo("category", room.getCategory())
        .whereEqualTo("district", room.getDistrict())
        .whereGreaterThanOrEqualTo("price", room.getPrice() - DELTA_PRICE)
        .whereLessThanOrEqualTo("price", room.getPrice() + DELTA_PRICE)
        .orderBy("price", Query.Direction.ASCENDING)
        .orderBy("count_view", Query.Direction.DESCENDING)
        .limit(LIMIT);

    recyclerView.setHasFixedSize(true);
    recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
    recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
      @Override
      public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        final int space = 4;
        final int position = parent.getChildAdapterPosition(view);
        if (position != NO_POSITION) {
          outRect.left = space;
          outRect.right = space;
          outRect.bottom = space;
          outRect.top = space;
        }
      }
    });


    final FirestoreRecyclerOptions<MotelRoom> motelRoomFirestoreRecyclerOptions = new FirestoreRecyclerOptions.Builder<MotelRoom>()
        .setQuery(query, snapshot -> FirebaseModel.documentSnapshotToObject(snapshot, MotelRoom.class))
        .build();
    adapter = new FirestoreRecyclerAdapter<MotelRoom, RelatedRoomItemVH>(motelRoomFirestoreRecyclerOptions) {
      @NonNull
      @Override
      public RelatedRoomItemVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RelatedRoomItemVH(
            LayoutInflater.from(parent.getContext()).inflate(R.layout.home_room_item_layout, parent, false),
            RelatedFragment.this::onClickListener);
      }

      @Override
      protected void onBindViewHolder(@NonNull RelatedRoomItemVH relatedRoomItemVH, int i, @NonNull MotelRoom motelRoom) {
        relatedRoomItemVH.bind(motelRoom);
      }

      @Override
      public void onDataChanged() {
        super.onDataChanged();
        progressbar.setVisibility(View.INVISIBLE);
      }
    };
    recyclerView.setAdapter(adapter);
    adapter.startListening();
  }

  private void onClickListener(int position) {
    if (adapter != null) {
      final String id = adapter.getItem(position).getId();
      final Context context = requireContext();
      final Intent intent = new Intent(context, DetailActivity.class);
      intent.putExtra(EXTRA_MOTEL_ROOM_ID, id);
      context.startActivity(intent);
    }
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    if (adapter != null) {
      adapter.stopListening();
    }
    unbinder.unbind();
  }
}
