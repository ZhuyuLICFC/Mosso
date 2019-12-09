package com.cs591.relativity.locsync;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.media.ImageReader;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback{

    private static final int RUNNING_ZOOM_IN = 19;

    private boolean mPermissionDenied = false;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final String TAG = "Main";
    private List<Marker> markers;
    private List<LatLng> polyInfo;

    //UI
    private ImageView ivSnapShot;
    private Polyline myTrack;

    private GoogleMap googleMap;
    private ConstraintLayout userConfig;
    private CheckBox mineLayer;
    private CheckBox othersLayer;
    private CheckBox trackLayer;


    // data / view initialization
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ivSnapShot = findViewById(R.id.ivSanpShot);
        markers = new ArrayList();
        userConfig = findViewById(R.id.layerSetting);
        userConfig.setVisibility(View.INVISIBLE);
        mineLayer = findViewById(R.id.cbMyLocation);
        othersLayer = findViewById(R.id.cbOthers);
        trackLayer = findViewById(R.id.cbPolyLine);
    }

    // check and enable location permission
    private void enableMyLocation() {
        // check if user have location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (googleMap != null) {
            // Access to the location has been granted to the app.
            googleMap.setMyLocationEnabled(true);
        }
    }

    // this method will be called when the map is loaded
    @Override
    public void onMapReady(GoogleMap map) {
        Log.d(TAG, "Here");
        LocationManager locationManager= (LocationManager)getSystemService(LOCATION_SERVICE);
        Location location = null;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            location = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
        }

        if (location == null) {
            Log.d(TAG, "null");
            return;
        }

        // locate user with specific zoom level
        LatLng ll = new LatLng(location.getLatitude(),location.getLongitude());
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, 19);
        // 1. assign map object
        googleMap = map;
        googleMap.animateCamera(update, new GoogleMap.CancelableCallback() {
            // 2. add callback method to make sure the adding of marker and polyline is just after zoom process
            @Override
            public void onFinish() {
                DataHelper.generateMarkers(getLocation());
                addMarkers();
                addPolyLines();
                googleMap.setMyLocationEnabled(false);
            }

            @Override
            public void onCancel() {

            }
        });
        // 3. enable my location layer
        googleMap.setOnMyLocationButtonClickListener(this);
        googleMap.setOnMyLocationClickListener(this);
        googleMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());
        hideMarkers();

        enableMyLocation();
    }

    // my location layer listener functions
    @Override
    public boolean onMyLocationButtonClick() {
        recenterLocation(getLocation(), RUNNING_ZOOM_IN);
        return true;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
    }

    // support functions for permission checking and asking
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    public void onScreenshot(View view) {
        googleMap.setMyLocationEnabled(false);
        hideMarkers();
        recenterLocation(checkBounds(polyInfo), new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                takeSnapshot();
                recenterLocation(getLocation(), RUNNING_ZOOM_IN, new GoogleMap.CancelableCallback() {
                    @Override
                    public void onFinish() {
                        googleMap.setMyLocationEnabled(true);
                        showMarkers();
                    }

                    @Override
                    public void onCancel() {

                    }
                });
            }
            @Override
            public void onCancel() {

            }
        });
    }

    // listener function for two floating action bar, which is early binding
    public void onOpen(View view) {
        googleMap.setMyLocationEnabled(true);
        //takeSnapshot();
    }

    public void onConfig(View view) {
        if (userConfig.getVisibility() == View.INVISIBLE) userConfig.setVisibility(View.VISIBLE);
        else userConfig.setVisibility(View.INVISIBLE);
    }

    // listener function for three check boxes, which is early binding
    public void onMineToggled(View view) {
        Log.d(TAG, mineLayer.isChecked() + "");
        googleMap.setMyLocationEnabled(mineLayer.isChecked());
    }

    public void onOthersToggled(View view) {
        Log.d(TAG, othersLayer.isChecked() + "");
        if (othersLayer.isChecked()) showMarkers();
        else hideMarkers();
    }

    public void onTrackToggled(View view) {
        if (trackLayer.isChecked()) showTrack();
        else hideTrack();
    }

    // listener function for screenshot button
    private void takeSnapshot() {
        // 1. check if the map is null (which means the map hasn't been loaded)
        if (googleMap == null) {
            return;
        }

        // 2. override the snapshot function
        final GoogleMap.SnapshotReadyCallback callback = new GoogleMap.SnapshotReadyCallback() {
            @Override
            public void onSnapshotReady(Bitmap snapshot) {
                // 2.1 Callback is called from the main thread, so we can modify the ImageView safely.
                ivSnapShot.setImageBitmap(snapshot);
            }
        };
        googleMap.snapshot(callback);
    }

    private Location getLocation() {
        LocationManager locationManager= (LocationManager)getSystemService(LOCATION_SERVICE);
        Location location = null;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            location = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
        }

        return location;
    }

    // three overloaded functions used to center user's location
    private void recenterLocation(Location location, int zoom) {
        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
        googleMap.animateCamera(update);
    }

    private void recenterLocation(Location location, int zoom, GoogleMap.CancelableCallback cancelableCallback) {
        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
        googleMap.animateCamera(update, cancelableCallback);
    }

    private void recenterLocation(LatLngBounds latLngBounds, GoogleMap.CancelableCallback cancelableCallback) {
        CameraUpdate update = CameraUpdateFactory.newLatLngBounds(latLngBounds, 20);
        googleMap.animateCamera(update, cancelableCallback);
    }

    // find the SW and NE point in polyline
    private LatLngBounds checkBounds(List<LatLng> latLngs) {
        double vMin = Integer.MAX_VALUE, vMax = Integer.MIN_VALUE, hMin = Integer.MAX_VALUE, hMax = Integer.MIN_VALUE;
        for (LatLng latLng : latLngs) {
            vMin = Math.min(latLng.longitude, vMin);
            vMax = Math.max(latLng.longitude, vMax);
            hMin = Math.min(latLng.latitude, hMin);
            hMax = Math.max(latLng.latitude, hMax);
        }
        return new LatLngBounds(new LatLng(hMin, vMin), new LatLng(hMax, vMax));
    }

    // initialize each markerinfo object as marker
    private void addMarkers() {
        for (MarkerInfo markerInfo : MarkerInfo.markerInfos) {
            markers.add(googleMap.addMarker(new MarkerOptions()
                    .position(markerInfo.getLatLng())
                    .title(markerInfo.getTitle())
                    .snippet(markerInfo.getInfo())
                    .icon(markerInfo.getColor())
                    .visible(true)));
        }
        hideMarkers();
    }

    // show and hide markers
    private void showMarkers() {
        for (Marker marker : markers) {
            marker.setVisible(true);
        }
    }

    private void hideMarkers() {
        for (Marker marker : markers) {
            marker.setVisible(false);
        }
    }


    private void addPolyLines() {
        // generate random polyline and assign to polyline object
        polyInfo = DataHelper.generatePolyLines(getLocation(), 10);

        // customize polyline
        myTrack = googleMap.addPolyline(new PolylineOptions()
                .addAll(polyInfo)
                .color(Color.RED)
                .visible(true));
        myTrack.setVisible(false);
    }

    // show and hide track, means show or hide the polyline object
    private void showTrack() {
        myTrack.setVisible(true);
    }

    private void hideTrack() {
        myTrack.setVisible(false);
    }


    // customize marker info window,
    class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private final View mWindow;

        private final View mContents;

        CustomInfoWindowAdapter() {
            mWindow = getLayoutInflater().inflate(R.layout.custom_info_window, null);
            mContents = getLayoutInflater().inflate(R.layout.custom_info_contents, null);
        }

        @Override
        public View getInfoWindow(Marker marker) {
            render(marker, mWindow);
            return mWindow;
        }

        @Override
        public View getInfoContents(Marker marker) {
//            render(marker, mContents);
//            return mContents;
            return null;
        }


        // the way is really like how you combine the data and layout when creating a custom listview
        private void render(Marker marker, View view) {

            ((ImageView)view.findViewById(R.id.badge)).setImageResource(R.drawable.mine);

            String title = marker.getTitle();
            TextView titleUi = ((TextView) view.findViewById(R.id.title));
            if (title != null) {
                // Spannable string allows us to edit the formatting of the text.
                SpannableString titleText = new SpannableString(title);
                titleText.setSpan(new ForegroundColorSpan(Color.RED), 0, titleText.length(), 0);
                titleUi.setText(titleText);
            } else {
                titleUi.setText("");
            }

            String snippet = marker.getSnippet();
            TextView snippetUi = ((TextView) view.findViewById(R.id.snippet));
            if (snippet != null && snippet.length() > 5) {
                SpannableString snippetText = new SpannableString(snippet);
                snippetUi.setText(snippetText);
            } else {
                snippetUi.setText("");
            }
        }
    }
}
