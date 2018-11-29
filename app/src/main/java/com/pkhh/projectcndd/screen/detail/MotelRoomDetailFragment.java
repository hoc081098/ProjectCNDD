package com.pkhh.projectcndd.screen.detail;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.daimajia.slider.library.Transformers.AccordionTransformer;
import com.daimajia.slider.library.Transformers.BackgroundToForegroundTransformer;
import com.daimajia.slider.library.Transformers.BaseTransformer;
import com.daimajia.slider.library.Transformers.CubeInTransformer;
import com.daimajia.slider.library.Transformers.DefaultTransformer;
import com.daimajia.slider.library.Transformers.DepthPageTransformer;
import com.daimajia.slider.library.Transformers.FadeTransformer;
import com.daimajia.slider.library.Transformers.FlipHorizontalTransformer;
import com.daimajia.slider.library.Transformers.FlipPageViewTransformer;
import com.daimajia.slider.library.Transformers.RotateDownTransformer;
import com.daimajia.slider.library.Transformers.RotateUpTransformer;
import com.daimajia.slider.library.Transformers.StackTransformer;
import com.daimajia.slider.library.Transformers.TabletTransformer;
import com.daimajia.slider.library.Transformers.ZoomInTransformer;
import com.daimajia.slider.library.Transformers.ZoomOutSlideTransformer;
import com.daimajia.slider.library.Transformers.ZoomOutTransformer;
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
import com.pkhh.projectcndd.screen.PhotoSlideActivity;
import com.pkhh.projectcndd.screen.profile.UserProfileActivity;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

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

public class MotelRoomDetailFragment extends Fragment {
  public final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
  public final NumberFormat viewCountFormat = new DecimalFormat("###,###");

  private static final BaseTransformer[] TRANSFORMERS = {
      new AccordionTransformer(),
      new BackgroundToForegroundTransformer(),
      new CubeInTransformer(),
      new FlipHorizontalTransformer(),
      new FlipPageViewTransformer(),
      new FadeTransformer(),
      new DepthPageTransformer(),
      new DefaultTransformer(),
      new CubeInTransformer(),
      new RotateDownTransformer(),
      new RotateUpTransformer(),
      new StackTransformer(),
      new TabletTransformer(),
      new ZoomInTransformer(),
      new ZoomOutSlideTransformer(),
      new ZoomOutTransformer()
  };
  private final Random random = new Random();


  private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

  @BindView(R.id.sliderLayout) SliderLayout sliderLayout;
  @BindView(R.id.text_price) TextView textPrice;
  @BindView(R.id.text_address) TextView textAddress;
  @BindView(R.id.text_post_time) TextView textTimePost;
  @BindView(R.id.text_area) TextView textArea;
  @BindView(R.id.image_map) ImageView imageMap;
  @BindView(R.id.text_name) TextView textName;
  @BindView(R.id.text_phone) TextView textPhone;
  @BindView(R.id.image_avatar) ImageView imageAvatar;
  @BindView(R.id.text_category) TextView textCategory;
  @BindView(R.id.text_title) TextView textTitle;
  @BindView(R.id.text_available) TextView textAvailable;
  @BindView(R.id.text_view_count) TextView textViewCount;


  @Nullable private User user;
  @Nullable private ListenerRegistration registration;
  private String id;

  public static MotelRoomDetailFragment newInstance(String id) {
    final MotelRoomDetailFragment fragment = new MotelRoomDetailFragment();
    final Bundle args = new Bundle();
    args.putString(EXTRA_MOTEL_ROOM_ID, id);
    fragment.setArguments(args);
    return fragment;
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_motel_room_detail, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    ButterKnife.bind(this, view);

    id = Objects.requireNonNull(getArguments()).getString(EXTRA_MOTEL_ROOM_ID);
    increaseViewCount(id);
  }

  private void getDetail(String id) {
    registration = firestore.document(ROOMS_NAME_COLLECION + "/" + id)
        .addSnapshotListener((queryDocumentSnapshots, e) -> {
          if (e != null) {
            Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
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
    }).addOnSuccessListener(newVal -> Timber.tag("@@@").d("update newVal=" + newVal + " count_view successfully"))
        .addOnFailureListener(e -> Timber.tag("@@@").d(e, "update count_view error: %s", e.getMessage()));
  }

  private void updateUi(@NonNull MotelRoom motelRoom) {
    // slider
    updateImageSlider(motelRoom);

    // card 1
    textPrice.setText(getString(R.string.detail_price, PRICE_FORMAT.format(motelRoom.getPrice())));
    textAddress.setText(motelRoom.getAddress());
    textTimePost.setText(getString(R.string.posted_date, dateFormat.format(motelRoom.getCreatedAt())));
    textArea.setText(HtmlCompat.fromHtml(motelRoom.getSize() + " m<sup><small>2</small></sup>", FROM_HTML_SEPARATOR_LINE_BREAK_DIV));
    final Point point = Point.fromLngLat(motelRoom.getAddressGeoPoint().getLongitude(), motelRoom.getAddressGeoPoint().getLatitude());
    final MapboxStaticMap staticImage = MapboxStaticMap.builder()
        .accessToken(getString(R.string.mapbox_access_token))
        .styleId(StaticMapCriteria.LIGHT_STYLE)
        .cameraPoint(point) // Image's center point on map
        .cameraZoom(9)
        .width(dpToPx(128)) // Image width
        .height(dpToPx(128)) // Image height
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

    // card 2
    motelRoom
        .getUser()
        .get()
        .addOnSuccessListener(requireActivity(), documentSnapshot -> {
          user = documentSnapshotToObject(documentSnapshot, User.class);
          updateUserInformation(user);
        })
        .addOnFailureListener(requireActivity(), e -> Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    textPhone.setText(motelRoom.getPhone());

    // card 3
    motelRoom
        .getCategory()
        .get()
        .addOnSuccessListener(requireActivity(), documentSnapshot -> {
          textCategory.setText(documentSnapshot.getString("name"));
        })
        .addOnFailureListener(requireActivity(), e -> Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    textTitle.setText(motelRoom.getTitle());
    textAvailable.setText(
        motelRoom.isActive() ? R.string.available_yes : R.string.available_no
    );
    textViewCount.setText(viewCountFormat.format(motelRoom.getCountView()));
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

      final BaseSliderView sliderView = new TextSliderView(requireContext())
          .description(getString(R.string.image, i + 1))
          .image(e)
          .setOnSliderClickListener(__ -> onSliderClick(index, motelRoom.getImages()))
          .setScaleType(BaseSliderView.ScaleType.FitCenterCrop);
      sliderLayout.addSlider(sliderView);
    }

    sliderLayout.setPagerTransformer(true, TRANSFORMERS[random.nextInt(TRANSFORMERS.length)]);
  }

  private void onSliderClick(int index, List<String> images) {
    final Intent intent = new Intent(requireContext(), PhotoSlideActivity.class);
    intent.putStringArrayListExtra(EXTRA_IMAGES, new ArrayList<>(images));
    intent.putExtra(EXTRA_INDEX, index);
    startActivity(intent);
  }

  @Override
  public void onResume() {
    super.onResume();
    getDetail(id);
    sliderLayout.startAutoCycle();
  }

  @Override
  public void onPause() {
    super.onPause();
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
        final Intent intent = new Intent(requireContext(), UserProfileActivity.class);
        intent.putExtra(EXTRA_USER_ID, user.getId());
        intent.putExtra(EXTRA_USER_FULL_NAME, user.getFullName());
        startActivity(intent);
      }
    }
  }

  private boolean isTelephonyEnabled() {
    TelephonyManager manager = (TelephonyManager) requireContext().getSystemService(Context.TELEPHONY_SERVICE);
    return manager != null && manager.getSimState() == TelephonyManager.SIM_STATE_READY;
  }

  private int dpToPx(int dp) {
    DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
    return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
  }
}
