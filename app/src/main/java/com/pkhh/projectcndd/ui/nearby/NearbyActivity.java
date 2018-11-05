package com.pkhh.projectcndd.ui.nearby;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.pkhh.projectcndd.R;
import com.pkhh.projectcndd.models.FirebaseModel;
import com.pkhh.projectcndd.models.MotelRoom;
import com.pkhh.projectcndd.utils.Constants;

import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class NearbyActivity extends AppCompatActivity implements OnMapReadyCallback, MapboxMap.OnMapClickListener, LocationEngineListener, PermissionsListener {
  private static final String TAG = "@@@@";

  private static final int INTERVAL = 5_000;
  private static final int FASTEST_INTERVAL = 3_000;
  private static final float SMALLEST_DISPLACEMENT = 10f;

  private MapView mapView;


  private MapboxMap mMap;
  private PermissionsManager permissionsManager;
  private LocationLayerPlugin locationLayerPlugin;

  @Nullable
  private Location originLocation;
  @Nullable
  private LatLng originCoord;

  @Nullable
  private Marker destinationMarker;
  @Nullable
  private DirectionsRoute currentRoute;
  private NavigationMapRoute navigationMapRoute;
  private Button button;
  private LocationEngine locationEngine;
  private List<Marker> markers;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Mapbox.getInstance(this, getString(R.string.access_token));
    setContentView(R.layout.activity_nearby);

    mapView = findViewById(R.id.map_view);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(MapboxMap mapboxMap) {
    this.mMap = mapboxMap;
    enableLocationPlugin();

    mapboxMap.addOnMapClickListener(this);
    button = findViewById(R.id.start_button);
    button.setOnClickListener(v -> {
      if (currentRoute != null) {

        boolean simulateRoute = true;
        NavigationLauncherOptions options = NavigationLauncherOptions.builder()
            .directionsRoute(currentRoute)
            .shouldSimulateRoute(simulateRoute)
            .build();
        // Call this method with Context from within an Activity
        NavigationLauncher.startNavigation(NearbyActivity.this, options);

      }
    });
  }

  @Override
  public void onMapClick(@NonNull LatLng point) {
    Toast.makeText(this, "Clicked: " + point, Toast.LENGTH_SHORT).show();
    Toast.makeText(this, "OriginCoord: " + originCoord, Toast.LENGTH_SHORT).show();

    if (destinationMarker != null) {
      mMap.removeMarker(destinationMarker);
    }
    destinationMarker = mMap.addMarker(
        new MarkerOptions()
            .position(point)
    );

    Point destinationPosition = Point.fromLngLat(point.getLongitude(), point.getLatitude());
    if (originCoord != null) {
      Point originPosition = Point.fromLngLat(originCoord.getLongitude(), originCoord.getLatitude());
      getRoute(originPosition, destinationPosition);

      button.setEnabled(true);
      button.setBackgroundResource(R.color.colorAccent);
    }

  }

  private void getRoute(@NonNull Point origin, @NonNull Point destination) {
    NavigationRoute.builder(this)
        .accessToken(Objects.requireNonNull(Mapbox.getAccessToken()))
        .origin(origin)
        .destination(destination)
        .build()
        .getRoute(new Callback<DirectionsResponse>() {

          @Override
          public void onResponse(@NonNull Call<DirectionsResponse> call, @NonNull Response<DirectionsResponse> response) {
            // You can get the generic HTTP info about the response
            Timber.tag(TAG).d("Response code: %s", response.code());
            if (response.body() == null) {
              Timber.tag(TAG).e("No routes found, make sure you set the right user and access token.");
              Toast.makeText(NearbyActivity.this, "No routes found, make sure you set the right user and access token.", Toast.LENGTH_SHORT).show();
              return;
            } else if (response.body().routes().size() < 1) {
              Toast.makeText(NearbyActivity.this, "No routes found", Toast.LENGTH_SHORT).show();
              Timber.tag(TAG).e("No routes found");
              return;
            }

            currentRoute = response.body().routes().get(0);

            // Draw the route on the map
            if (navigationMapRoute != null) {
              navigationMapRoute.removeRoute();
            } else {
              navigationMapRoute = new NavigationMapRoute(null, mapView, mMap, R.style.NavigationMapRoute);
            }
            navigationMapRoute.addRoute(currentRoute);
          }

          @Override
          public void onFailure(@NonNull Call<DirectionsResponse> call, @NonNull Throwable throwable) {
            Toast.makeText(NearbyActivity.this, "Error: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
            Timber.tag(TAG).e(throwable, "Error: %s", throwable.getMessage());
          }
        });
  }

  private void enableLocationPlugin() {
    // Check if permissions are enabled and if not request
    if (PermissionsManager.areLocationPermissionsGranted(this)) {
      initializeLocationEngine();

      locationLayerPlugin = new LocationLayerPlugin(mapView, mMap);
      locationLayerPlugin.setCameraMode(CameraMode.TRACKING);
      locationLayerPlugin.setRenderMode(RenderMode.NORMAL);
      Location testLocation = new Location("TEST LOCATION");
      testLocation.setLatitude(0);
      testLocation.setLongitude(0);
      locationLayerPlugin.forceLocationUpdate(testLocation);
    } else {
      permissionsManager = new PermissionsManager(this);
      permissionsManager.requestLocationPermissions(this);
    }
  }

  private void initializeLocationEngine() {
    locationEngine = new LocationEngineProvider(this).obtainBestLocationEngineAvailable();
    locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
    locationEngine.setFastestInterval(FASTEST_INTERVAL);
    locationEngine.setInterval(INTERVAL);
    locationEngine.setSmallestDisplacement(SMALLEST_DISPLACEMENT);
    locationEngine.activate();

    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
      Toast.makeText(this, "Location permission is not granted", Toast.LENGTH_SHORT).show();
      return;
    }
    locationEngine.addLocationEngineListener(this);
    locationEngine.requestLocationUpdates();

    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
      Toast.makeText(this, "Location permission is not granted", Toast.LENGTH_SHORT).show();
      return;
    }

    Location lastLocation = locationEngine.getLastLocation();
    if (lastLocation != null) {
      originLocation = lastLocation;
      originCoord = new LatLng(originLocation.getLatitude(), originLocation.getLongitude());
    }

  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }

  @Override
  public void onExplanationNeeded(List<String> permissionsToExplain) {
    Toast.makeText(this, Stream.of(permissionsToExplain).collect(Collectors.joining("\n")), Toast.LENGTH_SHORT).show();
    Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
  }

  @Override
  public void onPermissionResult(boolean granted) {
    if (granted) {
      enableLocationPlugin();
    } else {
      Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
      finish();
    }
  }

  @Override
  protected void onStart() {
    super.onStart();
    mapView.onStart();
    if (locationLayerPlugin != null) {
      locationLayerPlugin.onStart();
    }
    if (locationEngine != null) {
      if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
          && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        Toast.makeText(this, "Location permission is not granted", Toast.LENGTH_SHORT).show();
        return;
      }
      locationEngine.addLocationEngineListener(this);
      locationEngine.requestLocationUpdates();
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    mapView.onResume();
  }

  @Override
  protected void onPause() {
    super.onPause();
    mapView.onPause();
  }

  @Override
  protected void onStop() {
    super.onStop();
    mapView.onStop();
    if (locationLayerPlugin != null) {
      locationLayerPlugin.onStop();
    }

    if (locationEngine != null) {
      locationEngine.removeLocationEngineListener(this);
      locationEngine.removeLocationUpdates();
    }
  }


  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mapView.onDestroy();
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    mapView.onLowMemory();
  }

  @Override
  public void onConnected() {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
      //Toast.makeText(this, "Location permission is not granted", Toast.LENGTH_SHORT).show();
      return;
    }
    locationEngine.requestLocationUpdates();
    //Toast.makeText(this, "onConnected", Toast.LENGTH_SHORT).show();
  }

  @Override
  public void onLocationChanged(Location location) {
    originLocation = location;
    originCoord = new LatLng(originLocation.getLatitude(), originLocation.getLongitude());

    double distance = 10; // miles
    getDocumentNearBy(originCoord.getLatitude(), originCoord.getLongitude(), distance);

    Timber.tag(TAG).d("onLocationChanged: %s", location);
    //Toast.makeText(this, "onLocationChanged: " + location, Toast.LENGTH_SHORT).show();
  }

  private void getDocumentNearBy(double latitude, double longitude, double distance) {

    // ~1 mile of lat and lon in degrees
    double lat = 0.0144927536231884;
    double lon = 0.0181818181818182;

    double lowerLat = latitude - (lat * distance);
    double lowerLon = longitude - (lon * distance);

    double greaterLat = latitude + (lat * distance);
    double greaterLon = longitude + (lon * distance);

    GeoPoint lesserGeoPoint = new GeoPoint(lowerLat, lowerLon);
    GeoPoint greaterGeoPoint = new GeoPoint(greaterLat, greaterLon);

    CollectionReference docRef = FirebaseFirestore.getInstance().collection(Constants.ROOMS_NAME_COLLECION);
    Query query = docRef
        .whereGreaterThan("address_geopoint", lesserGeoPoint)
        .whereLessThan("address_geopoint", greaterGeoPoint);

    query.get()
        .addOnSuccessListener(queryDocumentSnapshots -> {
          if (this.markers != null) {
            for (Marker marker : this.markers) {
              mMap.removeMarker(marker);
            }
          }
          List<MarkerOptions> markers = Stream.of(FirebaseModel.querySnapshotToObjects(queryDocumentSnapshots, MotelRoom.class))
              .map(room -> new MarkerOptions()
                  .position(new LatLng(room.getAddressGeoPoint().getLatitude(), room.getAddressGeoPoint().getLongitude()))
                  .title(room.getTitle())
                  .icon(
                      IconFactory.getInstance(this)
                          .fromResource(R.drawable.map_marker_light)
                  )
                  .snippet(room.getDescription())
              )
              .toList();
          Toast.makeText(this, "getDocumentNearBy: {" + latitude + ", " + longitude + ", " + distance + "} --> " + markers.size(), Toast.LENGTH_SHORT).show();
          this.markers = mMap.addMarkers(markers);
        })
        .addOnFailureListener(e -> {
          Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
  }
}
