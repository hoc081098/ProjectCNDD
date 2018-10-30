package com.pkhh.projectcndd.ui.post;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

public class AddPhotoFragment extends Fragment implements RecyclerOnClickListener, View.OnClickListener {
    private static final int REQUEST_CODE_SELECT_IMAGE = 1;
    private final List<Uri> imageUris = new ArrayList<>();
    private final ImageAdapter mAdapter = new ImageAdapter(this, imageUris);

    @BindView(R.id.button_select_take_photo)
    View mSelectTakeImage;

    @BindView(R.id.recycler_img)
    RecyclerView mRecyclerViewImages;

    private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_photo, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        unbinder = ButterKnife.bind(this, view);

        mSelectTakeImage.setOnClickListener(this);
        mRecyclerViewImages.setHasFixedSize(true);
        mRecyclerViewImages.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        mRecyclerViewImages.setAdapter(mAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
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
                    Intent intentCamera = new Intent(requireContext(), Camera2Activity.class);
                    startActivityForResult(intentCamera, 1);
                    dialog.dismiss();
                    return;
                }

                if (button.getId() == R.id.button_select_image) {
                    dialog.dismiss();

                    final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
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
                imageUris.add(uri);
                mAdapter.notifyItemInserted(imageUris.size() - 1);
            }
        }
    }

    public List<Uri> getImageUris() {
        return imageUris;
    }

    @Override
    public void onClick(@NonNull View view, int position) {
        if (view.getId() == R.id.img_close) {
            imageUris.remove(position);
            mAdapter.notifyItemRemoved(position);
        } else if (view.getId() == R.id.img_preview) {
            Toast.makeText(requireContext(), "Clicked " + imageUris.get(position), Toast.LENGTH_SHORT).show();
        }
    }
}

