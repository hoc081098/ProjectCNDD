package com.pkhh.projectcndd.ui.post;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.pkhh.projectcndd.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static java.util.Objects.requireNonNull;

public class PickAddressActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {

    public static final String TAG = PickAddressActivity.class.getSimpleName();

    public static final int REQUEST_CODE_PERMISSION_LOCATION = 2;
    public static final int REQUEST_CHECK_SETTINGS = 3;
    public static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;

    public static final String EXTRA_ADDRESS = "EXTRA_ADDRESS";
    public static final String EXTRA_LATLNG = "EXTRA_LATLNG";

    @Nullable
    private GoogleMap mMap;
    private EditText mEditTextSearchBox;
    private View mImageCurrentLocation;
    private View mLayoutRelativePosition;
    private ImageView mImageRelative;

    @Nullable
    private FusedLocationProviderClient fusedLocationProviderClient;
    @Nullable
    private LocationCallback locationCallback;
    @Nullable
    private LocationRequest locationRequest;
    private boolean requestUpdateLocation;
    private boolean isCheckedRelative;
    @Nullable
    private Marker mMarker;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_address);

        final ActionBar supportActionBar = requireNonNull(getSupportActionBar());
        supportActionBar.setDisplayHomeAsUpEnabled(true);
        supportActionBar.setHomeAsUpIndicator(R.drawable.ic_done_white_24dp);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        requireNonNull(mapFragment).getMapAsync(this);

        findViews();
        setEvents();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (requestUpdateLocation && (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            if (fusedLocationProviderClient != null && locationCallback != null) {
                fusedLocationProviderClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        null
                );
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (locationCallback != null && fusedLocationProviderClient != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
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
        isCheckedRelative = !isCheckedRelative;
        final int color = ContextCompat.getColor(this, isCheckedRelative ? R.color.colorPrimary : android.R.color.white);
        mImageRelative.setImageDrawable(new ColorDrawable(color));
    }

    private void onClickImageCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION},
                    REQUEST_CODE_PERMISSION_LOCATION
            );
            return;
        }

        locationRequest = new LocationRequest()
                .setInterval(5_000)
                .setFastestInterval(3_000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setSmallestDisplacement(10f);

        LocationServices.getSettingsClient(this)
                .checkLocationSettings(
                        new LocationSettingsRequest.Builder()
                                .addLocationRequest(requireNonNull(locationRequest))
                                .build()
                )
                .addOnSuccessListener(__ -> initAndRequestLocationUpdate())
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

    private void initAndRequestLocationUpdate() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Log.d(TAG, "onLocationResult: " + locationResult.toString());

                final Location lastLocation = locationResult.getLastLocation();
                if (lastLocation != null && mMap != null) {
                    Log.d(TAG, "onLocationResult: " + lastLocation);

                    final LatLng latLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());

                    if (mMarker != null) {
                        mMarker.remove();
                    }
                    mMarker = mMap.addMarker(
                            new MarkerOptions()
                                    .position(latLng)
                                    .title("Vị trí của bạn")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_action_name))
                    );
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));

                    Geocoder geocoder = new Geocoder(PickAddressActivity.this, Locale.getDefault());
                    try {
                        List<Address> addresses = geocoder.getFromLocation(
                                lastLocation.getLatitude(),
                                lastLocation.getLongitude(),
                                1
                        );
                        String address = addresses.get(0).getAddressLine(0);
                        mEditTextSearchBox.setText(address);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                requireNonNull(locationCallback),
                null
        );
        requestUpdateLocation = true;
        Log.d(TAG, "requestUpdateLocation");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION_LOCATION) {
            if (checkGrantResults(grantResults)) {
                onClickImageCurrentLocation();
            } else {
                Toast.makeText(this, "Bạn nên cho phép truy cập quyển vị trí", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean checkGrantResults(@NonNull int[] grantResults) {
        if (grantResults.length == 0) {
            return false;
        }
        if (grantResults.length == 1) {
            return grantResults[0] == PERMISSION_GRANTED;
        }
        return grantResults[0] == PERMISSION_GRANTED || grantResults[1] == PERMISSION_GRANTED;
    }

    private void onClickEditSearchBox() {
        try {
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                    .build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE && data != null) {
            switch (resultCode) {
                case RESULT_OK:
                    Place place = PlaceAutocomplete.getPlace(this, data);
                    mEditTextSearchBox.setText(place.getAddress());

                    if (locationCallback != null && fusedLocationProviderClient != null) {
                        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
                    }
                    requestUpdateLocation = false;

                    if (mMarker != null) {
                        mMarker.remove();
                    }
                    if (mMap != null) {
                        final LatLng latLng = place.getLatLng();
                        mMarker = mMap.addMarker(
                                new MarkerOptions()
                                        .position(latLng)
                                        .title("Vị trí tìm kiếm")
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_action_name))
                        );
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
                    }

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finish() {
        final Intent data = new Intent();
        data.putExtra(EXTRA_ADDRESS, mEditTextSearchBox.getText());
        data.putExtra(EXTRA_LATLNG, mMarker != null ? mMarker.getPosition() : null);
        setResult(RESULT_OK, data);

        super.finish();
    }
}
