package com.pkhh.projectcndd.ui.post;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.pkhh.projectcndd.R;
import com.pkhh.projectcndd.utils.RecyclerOnClickListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

public class AddPhotoFragment extends Fragment implements RecyclerOnClickListener, View.OnClickListener {
  public static final int REQUEST_CODE_SELECT_IMAGE = 1;
  private final List<Uri> imageUris = new ArrayList<>();
  private View mSelectTakeImage;
  private RecyclerView mRecyclerViewImages;
  private ImageAdapter mAdapter = new ImageAdapter(this, imageUris);

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_add_photo, container, false);
  }

  @Override
  public void onClick(View v) {
    if (R.id.button_select_take_photo == v.getId()) {
      final View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_photo, null);
      final AlertDialog dialog = new AlertDialog.Builder(requireContext())
          .setView(view)
          .show();
      final View.OnClickListener onClickListener = button -> {
        if (button.getId() == R.id.button_cancel) {
          dialog.dismiss();
          return;
        }

        if (button.getId() == R.id.button_take_photo) {
          Toast.makeText(requireContext(), "TODO", Toast.LENGTH_SHORT).show();
          dialog.dismiss();
          return;
        }

        if (button.getId() == R.id.button_select_image) {
          dialog.dismiss();

          final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
          intent.setType("image/*");

          final Intent chooser = Intent.createChooser(intent, "Chọn ảnh");
          startActivityForResult(chooser, REQUEST_CODE_SELECT_IMAGE);
        }
      };
      view.findViewById(R.id.button_take_photo).setOnClickListener(onClickListener);
      view.findViewById(R.id.button_select_image).setOnClickListener(onClickListener);
      view.findViewById(R.id.button_cancel).setOnClickListener(onClickListener);
    }
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    initView(view);

    mSelectTakeImage.setOnClickListener(this);

    mRecyclerViewImages.setHasFixedSize(true);
    mRecyclerViewImages.setLayoutManager(new GridLayoutManager(requireContext(), 2));
    mRecyclerViewImages.setAdapter(mAdapter);
  }

  private void initView(View view) {
    mSelectTakeImage = view.findViewById(R.id.button_select_take_photo);
    mRecyclerViewImages = view.findViewById(R.id.recycler_img);
  }

  @Override
  public void onClick(@NonNull View view, int position) {
    if (view.getId() == R.id.img_close) {
      imageUris.remove(position);
      mAdapter.notifyItemRemoved(position);
      return;
    }

    Toast.makeText(requireContext(), "Clicked " + imageUris.get(position).getPath(), Toast.LENGTH_SHORT).show();
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == REQUEST_CODE_SELECT_IMAGE
        && resultCode == Activity.RESULT_OK
        && data != null) {
      final Uri uri = data.getData();
      if (uri != null) {
        imageUris.add(uri);
        mAdapter.notifyItemInserted(imageUris.size() - 1);
      }
    }
  }

  public List<Uri> getImageUris() {
    return imageUris;
  }
}

class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.VH> {
  private RecyclerOnClickListener mRecyclerOnClickListener;
  private List<Uri> mUris;

  ImageAdapter(RecyclerOnClickListener recyclerOnClickListener, List<Uri> uris) {
    mRecyclerOnClickListener = recyclerOnClickListener;
    mUris = uris;
  }

  @NonNull
  @Override
  public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_iteam_layout, parent, false);
    return new VH(view);
  }

  @Override
  public void onBindViewHolder(@NonNull VH holder, int position) {
    holder.bind(mUris.get(position));
  }

  @Override
  public int getItemCount() {
    return mUris.size();
  }

  class VH extends RecyclerView.ViewHolder implements View.OnClickListener {
    private ImageView mImageViewPreview;
    private ImageView mImageViewClose;

    public VH(@NonNull View itemView) {
      super(itemView);
      mImageViewPreview = itemView.findViewById(R.id.img_preview);
      mImageViewClose = itemView.findViewById(R.id.img_close);

      mImageViewPreview.setOnClickListener(this);
      mImageViewClose.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
      int adapterPosition = getAdapterPosition();
      if (adapterPosition != NO_POSITION) {
        mRecyclerOnClickListener.onClick(v, adapterPosition);
      }
    }

    public void bind(Uri uri) {
      Picasso.get()
          .load(uri)
          .fit()
          .centerCrop()
          .into(mImageViewPreview);
      Picasso.get()
          .load(R.drawable.ic_action_close)
          .fit()
          .centerCrop()
          .noFade()
          .into(mImageViewClose);
    }
  }
}
