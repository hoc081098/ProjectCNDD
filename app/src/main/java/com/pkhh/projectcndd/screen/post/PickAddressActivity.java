package com.pkhh.projectcndd.screen.post;

import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.annimon.stream.IntStream;
import com.annimon.stream.Stream;
import com.annimon.stream.function.Consumer;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mapbox.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.core.exceptions.ServicesException;
import com.mapbox.geojson.Point;
import com.pkhh.projectcndd.R;

import java.util.List;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.pkhh.projectcndd.utils.Constants.EXTRA_ADDRESS;
import static com.pkhh.projectcndd.utils.Constants.EXTRA_LATLNG;
import static java.util.Objects.requireNonNull;

/**
 * @author Peter Hoc
 * Created on 9/24/2018
 */

public class PickAddressActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {
  public static final String TAG = PickAddressActivity.class.getSimpleName();

  private static final String[] LOCATIONS_PERMISSION = {ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION};
  private static final int REQUEST_CODE_LOCATION_PERMISSION = 2;
  private static final int REQUEST_CHECK_SETTINGS = 3;

  private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
  private static final int INTERVAL = 3_000;
  private static final int FASTEST_INTERVAL = 1_000;
  private static final float SMALLEST_DISPLACEMENT = 10f;

  @Nullable private GoogleMap mMap;
  private EditText mEditTextSearchBox;
  private View mImageCurrentLocation;
  private View mLayoutRelativePosition;
  private ImageView mImageRelative;


  // keep track reference, we should initialize them once
  @Nullable private FusedLocationProviderClient _fusedLocationProviderClient;
  @Nullable private LocationCallback _locationCallback;
  @Nullable private LocationRequest _locationRequest;


  private boolean shouldRequestLocationUpdate;
  private boolean isCheckedRelative;
  private String mLatestMarkerTitle;

  // keep current location
  @Nullable private Marker mMarker;
  @Nullable private Circle mCircle;
  @Nullable private LatLng mLatLng;


  // input nhận từ SelectAddressLocationFragment
  @Nullable private LatLng mInputLatLng;
  @Nullable private CharSequence mInputAddress;


  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_pick_address);

    initActionBar();

    getInputFromIntent();

    getMap();

    findViews();

    setEvents();
  }

  private void getMap() {
    // Obtain the SupportMapFragment and get notified when the map is ready to be used.
    requireNonNull((SupportMapFragment) getSupportFragmentManager()
        .findFragmentById(R.id.map)).getMapAsync(this);
  }

  private void getInputFromIntent() {
    final Intent intent = getIntent();
    mInputAddress = intent.getCharSequenceExtra(EXTRA_ADDRESS);
    mInputLatLng = intent.getParcelableExtra(EXTRA_LATLNG);
  }

  private void initActionBar() {
    final ActionBar supportActionBar = requireNonNull(getSupportActionBar());
    supportActionBar.setDisplayHomeAsUpEnabled(true);
    supportActionBar.setHomeAsUpIndicator(R.drawable.ic_done_white_24dp);
  }

  @Override
  protected void onStart() {
    super.onStart();

    if (Stream.of(LOCATIONS_PERMISSION)
        .map(permission -> ContextCompat.checkSelfPermission(this, permission))
        .anyMatch(i -> i == PERMISSION_GRANTED)) {
      if (shouldRequestLocationUpdate) {
        getFusedLocationProviderClient().requestLocationUpdates(
            getLocationRequest(),
            getLocationCallback(),
            null /* Looper */
        );
      }
    }
  }

  @Override
  protected void onStop() {
    super.onStop();

    if (shouldRequestLocationUpdate) {
      Log.d(TAG, "removeLocationUpdates");
      getFusedLocationProviderClient().removeLocationUpdates(getLocationCallback());
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    if (shouldRequestLocationUpdate) {
      Log.d(TAG, "removeLocationUpdates");
      getFusedLocationProviderClient().removeLocationUpdates(getLocationCallback());
    }
  }

  private void setEvents() {
    mEditTextSearchBox.setOnClickListener(this);
    mImageCurrentLocation.setOnClickListener(this);
    mLayoutRelativePosition.setOnClickListener(this);
  }

  private void findViews() {
    mEditTextSearchBox = findViewById(R.id.edit_search_box);
    mImageCurrentLocation = findViewById(R.id.image_current_location);
    mLayoutRelativePosition = findViewById(R.id.layout_relative_position);
    mImageRelative = findViewById(R.id.image_relative);
  }


  /**
   * Manipulates the map once available.
   * This callback is triggered when the map is ready to be used.
   * This is where we can add markers or lines, add listeners or move the camera. In this case,
   * we just add a marker near Sydney, Australia.
   * If Google Play services is not installed on the device, the user will be prompted to install
   * it inside the SupportMapFragment. This method will only be triggered once the user has
   * installed Google Play services and returned to the app.
   */
  @Override
  public void onMapReady(GoogleMap googleMap) {
    mMap = googleMap;

    googleMap.setOnMapLongClickListener(latLng -> {
      // stop update current location
      shouldRequestLocationUpdate = false;
      getFusedLocationProviderClient().removeLocationUpdates(getLocationCallback());

      makeGeocodeSearch(new com.mapbox.mapboxsdk.geometry.LatLng(latLng.latitude, latLng.longitude),
          address -> updateSearchEditTextAndMap(address, latLng, "Vị trí bạn chọn"));
    });

    if (mInputAddress != null && mInputLatLng != null) {
      // show input address
      shouldRequestLocationUpdate = false;
      getFusedLocationProviderClient().removeLocationUpdates(getLocationCallback());
      updateSearchEditTextAndMap(mInputAddress, mInputLatLng, "Vị trí");
    }
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.edit_search_box:
        onClickEditSearchBox();
        break;
      case R.id.image_current_location:
        onClickImageCurrentLocation();
        break;
      case R.id.layout_relative_position:
        onClickLayoutRelativePosition();
        break;
    }
  }

  private void onClickLayoutRelativePosition() {
    if (mLatLng != null && mLatestMarkerTitle != null) {
      isCheckedRelative = !isCheckedRelative;
      mImageRelative.setImageResource(isCheckedRelative ? R.drawable.relative_checked : R.drawable.relative_unchecked);
      updateSearchEditTextAndMap(mEditTextSearchBox.getText(), mLatLng, mLatestMarkerTitle);
    }
  }

  private void onClickImageCurrentLocation() {
    if (Stream.of(LOCATIONS_PERMISSION)
        .map(permission -> ContextCompat.checkSelfPermission(this, permission))
        .allMatch(i -> i != PERMISSION_GRANTED)) {
      ActivityCompat.requestPermissions(
          this,
          LOCATIONS_PERMISSION,
          REQUEST_CODE_LOCATION_PERMISSION
      );
      return;
    }

    LocationServices.getSettingsClient(this)
        .checkLocationSettings(
            new LocationSettingsRequest.Builder()
                .addLocationRequest(getLocationRequest())
                .build()
        )
        .addOnSuccessListener(__ -> {
          // request location update
          shouldRequestLocationUpdate = true;
          getFusedLocationProviderClient().requestLocationUpdates(
              getLocationRequest(),
              getLocationCallback(),
              null
          );
        })
        .addOnFailureListener(e -> {
          Log.d(TAG, "checkLocationSettings: " + e.getMessage());
          if (e instanceof ResolvableApiException) {
            try {
              ((ResolvableApiException) e).startResolutionForResult(this, REQUEST_CHECK_SETTINGS);
            } catch (IntentSender.SendIntentException ex) {
              ex.printStackTrace();
            }
          }
        });
  }


  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == REQUEST_CODE_LOCATION_PERMISSION) {
      if (IntStream.of(grantResults).anyMatch(i -> i == PERMISSION_GRANTED)) {
        onClickImageCurrentLocation();
      } else {
        Toast.makeText(this, "Bạn nên cho phép truy cập quyển vị trí", Toast.LENGTH_SHORT).show();
      }
    }
  }

  private void onClickEditSearchBox() {
    try {
      Intent intent = new PlaceAutocomplete
          .IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
          .build(this);
      startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
    } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
      e.printStackTrace();
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE && data != null) {
      switch (resultCode) {
        case RESULT_OK:
          // stop update current location
          getFusedLocationProviderClient().removeLocationUpdates(getLocationCallback());
          shouldRequestLocationUpdate = false;

          Place place = PlaceAutocomplete.getPlace(this, data);
          updateSearchEditTextAndMap(place.getAddress(), place.getLatLng(), "Vị trí tìm kiếm");
          break;
        case PlaceAutocomplete.RESULT_ERROR:
          Status status = PlaceAutocomplete.getStatus(this, data);
          Toast.makeText(this, status.getStatusMessage(), Toast.LENGTH_SHORT).show();
          break;
        case RESULT_CANCELED:
          Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
          break;
      }
    }

    if (requestCode == REQUEST_CHECK_SETTINGS && resultCode == RESULT_OK) {
      onClickImageCurrentLocation();
    }
  }

  private void updateSearchEditTextAndMap(@Nullable CharSequence address, LatLng latLng, String markerTitle) {
    mEditTextSearchBox.setText(address);
    Timber.tag("%%%").d("updateSearchEditTextAndMap address = %s", address);

    if (mMarker != null) {
      mMarker.remove();
    }
    if (mCircle != null) {
      mCircle.remove();
    }


    this.mLatLng = latLng;
    this.mLatestMarkerTitle = markerTitle;


    if (mMap != null) {
      if (isCheckedRelative) {
        mCircle = mMap.addCircle(
            new CircleOptions()
                .center(latLng)
                .radius(100)
                .strokeWidth(0)
                .fillColor(ContextCompat.getColor(this, R.color.colorMaterialBlue400_Opacity80))
        );
      } else {
        mMarker = mMap.addMarker(
            new MarkerOptions()
                .position(latLng)
                .title(markerTitle)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_action_name))
        );
      }
      mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f));
    }
  }

  @NonNull
  @MainThread
  private FusedLocationProviderClient getFusedLocationProviderClient() {
    if (_fusedLocationProviderClient != null) {
      return _fusedLocationProviderClient;
    }
    return _fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
  }

  @MainThread
  @NonNull
  private LocationCallback getLocationCallback() {
    if (_locationCallback != null) {
      return _locationCallback;
    }
    return _locationCallback = new LocationCallback() {
      @Override
      public void onLocationResult(LocationResult locationResult) {
        super.onLocationResult(locationResult);
        Log.d(TAG, "onLocationResult: " + locationResult.toString());
        final Location lastLocation = locationResult.getLastLocation();

        if (lastLocation != null && mMap != null) {
          Log.d(TAG, "onLocationResult: " + lastLocation);

          final LatLng latLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
          makeGeocodeSearch(new com.mapbox.mapboxsdk.geometry.LatLng(latLng.latitude, latLng.longitude),
              address -> updateSearchEditTextAndMap(address, latLng, "Vị trí hiện tại"));
        }
      }
    };
  }

  private void makeGeocodeSearch(final com.mapbox.mapboxsdk.geometry.LatLng latLng, final Consumer<String> callback) {
    Timber.tag("%%%").d("makeGeocodeSearch %s", latLng);
    try {
      // Build a Mapbox geocoding request
      final MapboxGeocoding client = MapboxGeocoding.builder()
          .accessToken(getString(R.string.mapbox_access_token))
          .query(Point.fromLngLat(latLng.getLongitude(), latLng.getLatitude()))
          .geocodingTypes(GeocodingCriteria.TYPE_ADDRESS)
          .build();
      client.enqueueCall(new Callback<GeocodingResponse>() {
        @Override
        public void onResponse(@NonNull Call<GeocodingResponse> call, @NonNull Response<GeocodingResponse> response) {
          if (response.body() != null) {
            final List<CarmenFeature> results = response.body().features();
            if (results.size() > 0) {

              // Get the first Feature from the successful geocoding response
              final CarmenFeature feature = results.get(0);
              Timber.tag("%%%").d("CarmenFeature = %s", feature);
              Timber.tag("%%%").d("CarmenFeature placeName= %s", feature.placeName());
              callback.accept(feature.placeName());

            } else {
              Toast.makeText(PickAddressActivity.this, R.string.geocode_no_results, Toast.LENGTH_SHORT).show();
            }
          } else {
            Timber.tag("%%%").e("Response body is null");
            Toast.makeText(PickAddressActivity.this, "Response body is null", Toast.LENGTH_SHORT).show();
          }
        }

        @Override
        public void onFailure(@NonNull Call<GeocodingResponse> call, @NonNull Throwable throwable) {
          Timber.tag("%%%").e("Geocoding Failure: " + throwable.getMessage());
          Toast.makeText(PickAddressActivity.this, "Geocoding Failure: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
        }
      });
    } catch (ServicesException servicesException) {
      Timber.tag("%%%").e("Error geocoding: " + servicesException.toString());
      servicesException.printStackTrace();
      Toast.makeText(PickAddressActivity.this, "Error geocoding: " + servicesException.getMessage(), Toast.LENGTH_SHORT).show();
    }
  }

  @MainThread
  @NonNull
  private LocationRequest getLocationRequest() {
    if (_locationRequest != null) {
      return _locationRequest;
    }
    return _locationRequest = new LocationRequest()
        .setInterval(INTERVAL)
        .setFastestInterval(FASTEST_INTERVAL)
        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        .setSmallestDisplacement(SMALLEST_DISPLACEMENT);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      onBackPressed();
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void finish() {

    final CharSequence address = mEditTextSearchBox.getText();

    if (TextUtils.isEmpty(address) || mLatLng == null) {
      setResult(RESULT_CANCELED);
    } else {
      final Intent data = new Intent();
      data.putExtra(EXTRA_ADDRESS, address);
      data.putExtra(EXTRA_LATLNG, mLatLng);
      setResult(RESULT_OK, data);
    }
    super.finish();
  }
}
