package com.pkhh.projectcndd.ui.post;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.pkhh.projectcndd.R;

import java.util.Objects;

import androidx.fragment.app.FragmentActivity;

public class PickAddressActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap mMap;

    private EditText mEditTextSearchBox;
    private View mImageCurrentLocation;
    private View mLayoutRelativePosition;
    private CheckBox mCheckBoxRelative;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_address);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        Objects.requireNonNull(mapFragment).getMapAsync(this);

        findViews();
        setEvents();
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
        mCheckBoxRelative = findViewById(R.id.checkbox_relative);
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

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
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
        mCheckBoxRelative.setChecked(!mCheckBoxRelative.isChecked());
    }

    private void onClickImageCurrentLocation() {

    }

    private void onClickEditSearchBox() {

    }
}
