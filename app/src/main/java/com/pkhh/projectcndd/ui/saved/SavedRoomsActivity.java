package com.pkhh.projectcndd.ui.saved;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pkhh.projectcndd.R;
import com.pkhh.projectcndd.models.FirebaseModel;
import com.pkhh.projectcndd.models.MotelRoom;
import com.pkhh.projectcndd.utils.Constants;

import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

class MotelRoomAdapter extends ListAdapter<MotelRoom, MotelRoomAdapter.ViewHolder> {

  private static final DiffUtil.ItemCallback<MotelRoom> DIFF_CALLBACK = new DiffUtil.ItemCallback<MotelRoom>() {
    @Override
    public boolean areItemsTheSame(@NonNull MotelRoom oldItem, @NonNull MotelRoom newItem) {
      return oldItem.getId().equals(newItem.getId());
    }

    @Override
    public boolean areContentsTheSame(@NonNull MotelRoom oldItem, @NonNull MotelRoom newItem) {
      return oldItem.equals(newItem);
    }
  };

  MotelRoomAdapter() {
    super(DIFF_CALLBACK);
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.province_item_layout, parent, false);
    return new ViewHolder(itemView);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    holder.bind(getItem(position));
  }

  class ViewHolder extends RecyclerView.ViewHolder {
    ViewHolder(@NonNull View itemView) {
      super(itemView);
    }

    void bind(MotelRoom item) {
      itemView.<TextView>findViewById(R.id.text_province_name).setText(item.getPrice() + "--" + item.getAddress());
    }
  }
}

public final class SavedRoomsActivity extends AppCompatActivity {
  MotelRoomAdapter adapter = new MotelRoomAdapter();
  private RecyclerView recycler;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_saved_room);

    Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

    recycler = findViewById(R.id.recycler);

    recycler.setHasFixedSize(true);
    recycler.setLayoutManager(new LinearLayoutManager(this));
    recycler.setAdapter(adapter);

    FirebaseFirestore.getInstance()
        .collection(Constants.MOTEL_ROOM_NAME_COLLECION)
        .whereArrayContains("user_ids_saved", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
        .get()
        .addOnSuccessListener(queryDocumentSnapshots -> {
          final List<MotelRoom> motelRooms = FirebaseModel.querySnapshotToObjects(queryDocumentSnapshots, MotelRoom.class);
          adapter.submitList(motelRooms);
        })
        .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
  }
}
