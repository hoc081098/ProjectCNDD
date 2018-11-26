package com.pkhh.projectcndd.screen.detail;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
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
import com.google.firebase.firestore.ListenerRegistration;
import com.mapbox.api.staticmap.v1.MapboxStaticMap;
import com.mapbox.api.staticmap.v1.StaticMapCriteria;
import com.mapbox.api.staticmap.v1.models.StaticMarkerAnnotation;
import com.mapbox.geojson.Point;
import com.pkhh.projectcndd.R;
import com.pkhh.projectcndd.models.MotelRoom;
import com.pkhh.projectcndd.models.User;
import com.pkhh.projectcndd.screen.profile.UserProfileActivity;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static androidx.core.text.HtmlCompat.FROM_HTML_SEPARATOR_LINE_BREAK_DIV;
import static com.mapbox.api.staticmap.v1.StaticMapCriteria.MEDIUM_PIN;
import static com.pkhh.projectcndd.models.FirebaseModel.documentSnapshotToObject;
import static com.pkhh.projectcndd.screen.home.MotelRoomVH.PRICE_FORMAT;
import static com.pkhh.projectcndd.utils.Constants.EXTRA_IMAGES;
import static com.pkhh.projectcndd.utils.Constants.EXTRA_INDEX;
import static com.pkhh.projectcndd.utils.Constants.EXTRA_MOTEL_ROOM_ID;
import static com.pkhh.projectcndd.utils.Constants.EXTRA_USER_FULL_NAME;
import static com.pkhh.projectcndd.utils.Constants.EXTRA_USER_ID;
import static com.pkhh.projectcndd.utils.Constants.ROOMS_NAME_COLLECION;

public class MotelRoomDetailActivity extends AppCompatActivity {
  public final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
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

  private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

  @Nullable private User user;
  @Nullable private ListenerRegistration registration;
  private String id;

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


    id = getIntent().getStringExtra(EXTRA_MOTEL_ROOM_ID);
    increaseViewCount(id);
    getDetail(id);
  }

  private void getDetail(String id) {
    registration = firestore.document(ROOMS_NAME_COLLECION + "/" + id)
        .addSnapshotListener((queryDocumentSnapshots, e) -> {
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
    }).addOnSuccessListener(newVal -> Log.d("@@@", "update newVal=" + newVal + " count_view successfully"))
        .addOnFailureListener(e -> Log.d("@@@", "update count_view error: " + e.getMessage(), e));
  }

  private void updateUi(@NonNull MotelRoom motelRoom) {
    updateImageSlider(motelRoom);

    textPrice.setText("$ " + PRICE_FORMAT.format(motelRoom.getPrice()) + " đ");
    textAddress.setText(motelRoom.getAddress());
    textTimePost.setText("Ngày đăng: " + dateFormat.format(motelRoom.getCreatedAt()));
    textArea.setText(HtmlCompat.fromHtml(motelRoom.getSize() + " m<sup><small>2</small></sup>",
        FROM_HTML_SEPARATOR_LINE_BREAK_DIV));


    final Point point = Point.fromLngLat(motelRoom.getAddressGeoPoint().getLongitude(), motelRoom.getAddressGeoPoint().getLatitude());
    final MapboxStaticMap staticImage = MapboxStaticMap.builder()
        .accessToken(getString(R.string.mapbox_access_token))
        .styleId(StaticMapCriteria.LIGHT_STYLE)
        .cameraPoint(point) // Image's centerpoint on map
        .cameraZoom(13)
        .width(dpToPx(92)) // Image width
        .height(dpToPx(92)) // Image height
        .retina(false) // Retina 2x image will be returned
        .staticMarkerAnnotations(
            Collections.singletonList(
                StaticMarkerAnnotation.builder()
                    .color("ef5350")
                    .name(MEDIUM_PIN)
                    .label("a")
                    .lnglat(point)
                    .build()
            )
        )
        .build();
    Picasso.get()
        .load(staticImage.url().toString())
        .into(imageMap);

    motelRoom.getUser()
        .get()
        .addOnSuccessListener(documentSnapshot -> {
          user = documentSnapshotToObject(documentSnapshot, User.class);
          updateUserInformation(user);
        })
        .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());

    textPhone.setText(motelRoom.getPhone());
  }

  private void updateUserInformation(@NonNull User user) {
    if (!TextUtils.isEmpty(user.getAvatar())) {
      Picasso.get()
          .load(user.getAvatar())
          .fit()
          .centerCrop()
          .noFade()
          .into(imageAvatar);
    }
    textName.setText(user.getFullName());
  }

  private void updateImageSlider(@NonNull MotelRoom motelRoom) {
    sliderLayout.removeAllSliders();

    List<String> images = motelRoom.getImages();
    for (int i = 0; i < images.size(); i++) {
      final int index = i;
      final String e = images.get(i);

      final BaseSliderView sliderView = new TextSliderView(this)
          .description("Ảnh " + (i + 1))
          .image(e)
          .setOnSliderClickListener(__ -> onSliderClick(index, motelRoom.getImages()))
          .setScaleType(BaseSliderView.ScaleType.FitCenterCrop);
      sliderLayout.addSlider(sliderView);
    }
  }

  private void onSliderClick(int index, List<String> images) {
    final Intent intent = new Intent(this, PhotoSlideActivity.class);
    intent.putStringArrayListExtra(EXTRA_IMAGES, new ArrayList<>(images));
    intent.putExtra(EXTRA_INDEX, index);
    startActivity(intent);
  }

  @Override
  protected void onStart() {
    super.onStart();

    getDetail(id);
    sliderLayout.startAutoCycle();
  }

  @Override
  protected void onStop() {
    super.onStop();

    if (registration != null) {
      registration.remove();
    }
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

  public int dpToPx(int dp) {
    DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
    return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
  }
}
