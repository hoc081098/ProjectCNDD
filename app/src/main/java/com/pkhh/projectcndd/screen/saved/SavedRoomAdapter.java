package com.pkhh.projectcndd.screen.saved;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.annimon.stream.function.Consumer;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.pkhh.projectcndd.R;
import com.pkhh.projectcndd.models.MotelRoom;
import com.pkhh.projectcndd.utils.RecyclerOnClickListener;

import androidx.annotation.NonNull;

public class SavedRoomAdapter extends FirestoreRecyclerAdapter<MotelRoom, SavedViewHolder> {

  @NonNull private final RecyclerOnClickListener onClickListener;
  @NonNull private final Consumer<Integer> onDataChangedCb;

  SavedRoomAdapter(@NonNull FirestoreRecyclerOptions<MotelRoom> options,
                   @NonNull RecyclerOnClickListener onClickListener,
                   @NonNull Consumer<Integer> onDataChangedCb) {
    super(options);
    this.onClickListener = onClickListener;
    this.onDataChangedCb = onDataChangedCb;
  }

  @NonNull
  @Override
  public SavedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.saved_room_item_layout, parent, false);
    return new SavedViewHolder(itemView, onClickListener);
  }

  @Override
  protected void onBindViewHolder(@NonNull SavedViewHolder viewHolder, int i, @NonNull MotelRoom motelRoom) {
    viewHolder.bind(motelRoom);
  }

  @Override
  public void onDataChanged() {
    onDataChangedCb.accept(getItemCount());
  }
}
