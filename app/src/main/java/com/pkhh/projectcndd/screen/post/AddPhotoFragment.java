package com.pkhh.projectcndd.screen.post;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.pkhh.projectcndd.R;
import com.pkhh.projectcndd.utils.RecyclerOnClickListener;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;

public class AddPhotoFragment extends StepFragment<ImagesPhotosFragmentOutput> implements RecyclerOnClickListener, View.OnClickListener {
  private static final int REQUEST_CODE_SELECT_IMAGE = 1;
  private static final int REQUEST_IMAGE_CAPTURE = 2;
  private static final String CAPTURE_IMAGE_FILE_PROVIDER = "your.package.name.fileprovider";

  private ImageAdapter mAdapter;

  @BindView(R.id.button_select_take_photo)
  View mSelectTakeImage;

  @BindView(R.id.recycler_img)
  RecyclerView mRecyclerViewImages;

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    mSelectTakeImage.setOnClickListener(this);
    mRecyclerViewImages.setHasFixedSize(true);
    mRecyclerViewImages.setLayoutManager(new GridLayoutManager(requireContext(), 2));
    mAdapter = new ImageAdapter(this, getDataOutput().getUris());
    mRecyclerViewImages.setAdapter(mAdapter);
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
          dialog.dismiss();
          final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
          intent.setType("image/*");
          startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
          final Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
          startActivity(intentCamera);
          return;
        }

        if (button.getId() == R.id.button_select_image) {
          dialog.dismiss();

          final Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
          intent.setType("image/*");

          startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
        }
      };
      view.findViewById(R.id.button_take_photo).setOnClickListener(onClickListener);
      view.findViewById(R.id.button_select_image).setOnClickListener(onClickListener);
      view.findViewById(R.id.button_cancel).setOnClickListener(onClickListener);
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == REQUEST_CODE_SELECT_IMAGE
        && resultCode == Activity.RESULT_OK
        && data != null) {
      final Uri uri = data.getData();
      if (uri != null) {
        getDataOutput().getUris().add(uri);
        mAdapter.notifyItemInserted(getDataOutput().getUris().size() - 1);
      }
    }
    if (requestCode == REQUEST_IMAGE_CAPTURE
        && resultCode == Activity.RESULT_OK
        && data != null) {
      final Uri uri = data.getData();
      if (uri != null) {
        getDataOutput().getUris().add(uri);
        mAdapter.notifyItemInserted(getDataOutput().getUris().size() - 1);
      }
    }
  }

  @Override
  public void onClick(@NonNull View view, int position) {
    if (view.getId() == R.id.img_close) {
      getDataOutput().getUris().remove(position);
      mAdapter.notifyItemRemoved(position);
    } else if (view.getId() == R.id.img_preview) {
      Toast.makeText(requireContext(), "Clicked " + getDataOutput().getUris().get(position), Toast.LENGTH_SHORT).show();
    }
  }

  @NotNull
  @Override
  public ImagesPhotosFragmentOutput initialData() {
    return new ImagesPhotosFragmentOutput();
  }

  @Override
  protected void onInvalid() {
    super.onInvalid();
    Snackbar.make(Objects.requireNonNull(getView()), "Hãy chọn ít nhất 3 ảnh!", Snackbar.LENGTH_SHORT).show();
  }

  @Override
  public boolean isInvalidData() {
    return getDataOutput().getUris().size() < 3;
  }

  @Override
  public int getLayoutId() {
    return R.layout.fragment_add_photo;
  }
}

