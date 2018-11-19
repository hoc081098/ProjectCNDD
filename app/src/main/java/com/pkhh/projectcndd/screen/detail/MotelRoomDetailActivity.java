package com.pkhh.projectcndd.screen.detail;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pkhh.projectcndd.R;
import com.pkhh.projectcndd.models.MotelRoom;
import com.pkhh.projectcndd.models.User;
import com.pkhh.projectcndd.screen.profile.UserProfileActivity;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static androidx.core.text.HtmlCompat.FROM_HTML_SEPARATOR_LINE_BREAK_DIV;
import static com.pkhh.projectcndd.models.FirebaseModel.documentSnapshotToObject;
import static com.pkhh.projectcndd.screen.home.MotelRoomVH.decimalFormat;
import static com.pkhh.projectcndd.utils.Constants.MOTEL_ROOM_ID;
import static com.pkhh.projectcndd.utils.Constants.ROOMS_NAME_COLLECION;

public class MotelRoomDetailActivity extends AppCompatActivity {
  public static final String TAG = MotelRoomDetailActivity.class.getSimpleName();
  @SuppressLint("SimpleDateFormat")
  public static final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");

  public static final String EXTRA_USER_ID = "user_id";
  public static final String EXTRA_USER_FULL_NAME = "user_name";

  private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

  @BindView(R.id.collapsing_toolbar_layout) CollapsingToolbarLayout collapsingToolbarLayout;
  @BindView(R.id.sliderLayout) SliderLayout sliderLayout;
  @BindView(R.id.text_price) TextView textPrice;
  @BindView(R.id.text_address) TextView textAddress;
  @BindView(R.id.text_post_time) TextView textTimePost;
  @BindView(R.id.text_area) TextView textArea;
  @BindView(R.id.image_map) ImageView imageMap;
  @BindView(R.id.text_name) TextView textName;
  @BindView(R.id.text_phone) TextView textPhone;
  @BindView(R.id.image_avatar) ImageView imageAvatar;

  @Nullable
  private User user;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getWindow().setFlags(
        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
    );
    setContentView(R.layout.activity_motel_room_detail);

    ButterKnife.bind(this, this);
    collapsingToolbarLayout.setTitle(null);

    String id = getIntent().getStringExtra(MOTEL_ROOM_ID);
    increaseViewCount(id);
    firestore.document(ROOMS_NAME_COLLECION + "/" + id)
        .addSnapshotListener(this, (queryDocumentSnapshots, e) -> {
          if (e != null) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
          }
          if (queryDocumentSnapshots != null) {
            updateUi(documentSnapshotToObject(queryDocumentSnapshots, MotelRoom.class));
          }
        });
  }

  private void increaseViewCount(final String id) {
    firestore.runTransaction(transaction -> {
      final DocumentReference documentRef = firestore.document(ROOMS_NAME_COLLECION + "/" + id);
      Long viewCount = (Long) transaction.get(documentRef).get("count_view");
      if (viewCount == null) viewCount = 0L;

      transaction.update(documentRef, "count_view", viewCount + 1);
      return viewCount + 1;
    }).addOnSuccessListener(newVal -> {
      Log.d("@@@", "update newVal=" + newVal + " count_view successfully");
    }).addOnFailureListener(e -> {
      Log.d("@@@", "update count_view error: " + e.getMessage(), e);
    });
  }

  private void updateUi(@NonNull MotelRoom motelRoom) {
    Log.d(TAG, motelRoom.toString());
    updateImageSlider(motelRoom);

    textPrice.setText("$ " + decimalFormat.format(motelRoom.getPrice()) + " đ");
    textAddress.setText(motelRoom.getAddress());
    textTimePost.setText("Ngày đăng: " + dateFormat.format(motelRoom.getCreatedAt()));
    textArea.setText(HtmlCompat.fromHtml(motelRoom.getSize() + " m<sup><small>2</small></sup>",
        FROM_HTML_SEPARATOR_LINE_BREAK_DIV));
    //imageMap.setImageResource(R.drawable.ic_home_black_24dp); // TODO

    motelRoom.getUser().get()
        .addOnSuccessListener(documentSnapshot -> {
          user = documentSnapshotToObject(documentSnapshot, User.class);
          updateUserInformation(user);
        })
        .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());

    textPhone.setText(motelRoom.getPhone());
  }

  private void updateUserInformation(@NonNull User user) {
    Picasso.get()
        .load(user.getAvatar())
        .fit()
        .centerCrop()
        .noFade()
        .into(imageAvatar);
    textName.setText(user.getFullName());
  }

  private void updateImageSlider(@NonNull MotelRoom motelRoom) {
    sliderLayout.removeAllSliders();

    int index = 0;
    for (String e : motelRoom.getImages()) {
      BaseSliderView sliderView = new TextSliderView(this)
          .description("Ảnh " + ++index)
          .image(e)
          .setOnSliderClickListener(slider -> setOnSliderClickListener(slider.getUrl()))
          .setScaleType(BaseSliderView.ScaleType.Fit);
      sliderLayout.addSlider(sliderView);
    }
  }

  private void setOnSliderClickListener(String url) {
    // TODO PhotoView
    Toast.makeText(this, url, Toast.LENGTH_SHORT).show();
  }

  @Override
  protected void onStart() {
    super.onStart();
    sliderLayout.startAutoCycle();
  }

  @Override
  protected void onStop() {
    super.onStop();
    sliderLayout.stopAutoCycle();
  }

  @OnClick(value = {
      R.id.button_call,
      R.id.button_sms,
      R.id.image_avatar,
  })
  public void onClick(View v) {
    final int id = v.getId();

    if (id == R.id.button_call) {
      if (user != null && isTelephonyEnabled()) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + user.getPhone()));
        startActivity(intent);
      }
      return;
    }

    if (id == R.id.button_sms) {
      if (user != null) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setType("vnd.android-dir/mms-sms");
        intent.putExtra("address", user.getPhone());
        intent.putExtra("sms_body", "Nice");
        startActivity(intent);
      }
      return;
    }

    if (id == R.id.image_avatar) {
      if (user != null) {
        final Intent intent = new Intent(this, UserProfileActivity.class);
        intent.putExtra(EXTRA_USER_ID, user.getId());
        intent.putExtra(EXTRA_USER_FULL_NAME, user.getFullName());
        startActivity(intent);
      }
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      finish();
    }
    return super.onOptionsItemSelected(item);
  }

  private boolean isTelephonyEnabled() {
    TelephonyManager manager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
    return manager != null && manager.getSimState() == TelephonyManager.SIM_STATE_READY;
  }
}
