package com.pkhh.projectcndd.screen.post;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.pkhh.projectcndd.R;
import com.pkhh.projectcndd.screen.PhotoSlideActivity;
import com.pkhh.projectcndd.utils.Constants;
import com.pkhh.projectcndd.utils.RecyclerOnClickListener;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import timber.log.Timber;

public class AddPhotoFragment extends StepFragment<ImagesPhotosFragmentOutput> implements RecyclerOnClickListener, View.OnClickListener {
  private static final int REQUEST_CODE_SELECT_IMAGE = 1;
  private static final int REQUEST_IMAGE_CAPTURE = 2;
  private static final String CAPTURE_IMAGE_FILE_PROVIDER = "com.pkhh.projectcndd.fileprovider";
  public static final int WRITE_EXTERNAL_REQUEST_CODE = 3;

  @BindView(R.id.button_select_take_photo) View mSelectTakeImage;
  @BindView(R.id.recycler_img) RecyclerView mRecyclerViewImages;

  private ImageAdapter mAdapter;
  @Nullable private Uri photoURI = null;

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    mSelectTakeImage.setOnClickListener(this);
    mRecyclerViewImages.setHasFixedSize(true);
    mRecyclerViewImages.setLayoutManager(new GridLayoutManager(requireContext(), 2));
    mAdapter = new ImageAdapter(this, getDataOutput().getUris());
    mRecyclerViewImages.setAdapter(mAdapter);
  }

  private File createImageFile() throws IOException {
    // Create an image file name
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    String imageFileName = "JPEG_" + timeStamp + "_";
    File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    return File.createTempFile(
        imageFileName,  /* prefix */
        ".jpg",    /* suffix */
        storageDir      /* directory */
    );
  }

  @Override
  public void onClick(View v) {
    if (R.id.button_select_take_photo == v.getId()) {
      final View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_photo, null);
      final AlertDialog dialog = new AlertDialog.Builder(requireContext())
          .setView(view)
          .show();
      final View.OnClickListener onClickListener = button -> {
        final int id = button.getId();
        switch (id) {
          case R.id.button_cancel:
            dialog.dismiss();
            return;
          case R.id.button_take_photo:
            dialog.dismiss();
            takePhoto();
            return;
          case R.id.button_select_image:
            dialog.dismiss();
            selectImage();
            return;
        }
      };

      view.findViewById(R.id.button_take_photo).setOnClickListener(onClickListener);
      view.findViewById(R.id.button_select_image).setOnClickListener(onClickListener);
      view.findViewById(R.id.button_cancel).setOnClickListener(onClickListener);
    }
  }

  private void selectImage() {
    final Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
    intent.setType("image/*");
    startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
  }

  private void takePhoto() {
    if (!requireContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
      Snackbar.make(Objects.requireNonNull(getView()), R.string.have_not_camera, Snackbar.LENGTH_SHORT).show();
      return;
    }

    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    // Ensure that there's a camera activity to handle the intent
    if (takePictureIntent.resolveActivity(requireContext().getPackageManager()) != null) {
      // Create the File where the photo should go
      if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_REQUEST_CODE);
        return;
      }

      createFileAndStartCamera(takePictureIntent);
    } else {
      Timber.tag("&&&").d("resolveActivity null");
      Toast.makeText(requireContext(), R.string.have_not_camera_app, Toast.LENGTH_SHORT).show();
    }
  }

  private void createFileAndStartCamera(Intent takePictureIntent) {
    File photoFile;
    try {
      photoFile = createImageFile();
    } catch (IOException ex) {
      // Error occurred while creating the File
      Timber.tag("&&&").e(ex);
      Toast.makeText(requireContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
      return;
    }
    // Continue only if the File was successfully created
    if (photoFile != null) {
      photoURI = FileProvider.getUriForFile(
          requireContext(),
          CAPTURE_IMAGE_FILE_PROVIDER,
          photoFile
      );
      Timber.tag("&&&").d("startActivityForResult uri = %s", photoURI);
      takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
      startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == WRITE_EXTERNAL_REQUEST_CODE) {
      if (grantResults.length > 0) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          createFileAndStartCamera(new Intent(MediaStore.ACTION_IMAGE_CAPTURE));
        }
      }
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
    if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
      final Uri uri = photoURI;
      if (uri != null) {
        Timber.tag("&&&").d("onActivityResult %s", uri);
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

      final Intent intent = new Intent(requireContext(), PhotoSlideActivity.class);
      intent.putExtra(Constants.EXTRA_IMAGES_URIS, new ArrayList<>(getDataOutput().getUris()));
      intent.putExtra(Constants.EXTRA_INDEX, position);
      startActivity(intent);

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
    Snackbar.make(Objects.requireNonNull(getView()), getString(R.string.select_at_least_3_photos), Snackbar.LENGTH_SHORT).show();
  }

  @Override
  public boolean isInvalidData() {
    return getDataOutput().getUris().size() < 3;
  }

  @Override
  public int getLayoutId() { return R.layout.fragment_add_photo; }
}

