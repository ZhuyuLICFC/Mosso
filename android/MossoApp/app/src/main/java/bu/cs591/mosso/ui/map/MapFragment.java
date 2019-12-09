package bu.cs591.mosso.ui.map;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import bu.cs591.mosso.BuildConfig;
import bu.cs591.mosso.MainActivity;
import bu.cs591.mosso.R;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import bu.cs591.mosso.Utils;
import bu.cs591.mosso.db.MapMarker;
import bu.cs591.mosso.db.RunningRepo;
import bu.cs591.mosso.entity.CurrentUser;
import bu.cs591.mosso.entity.MarkerInfo;
import bu.cs591.mosso.entity.RunningParam;
import bu.cs591.mosso.entity.RunningRecord;
import bu.cs591.mosso.utils.DateHelper;
import bu.cs591.mosso.utils.ImageHelper;

public class MapFragment extends Fragment implements OnMapReadyCallback{

    // configuration info
    private static final String TAG = "testo";
    private boolean locationPermission;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private GoogleMap googleMap;
    private static final int DEFAULT_ZOOM = 15;
    private static final int RUNNING_ZOOM = 18;
    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);
    private LocationManager locationManager;

    // UI
    private Chip chipRed;
    private Chip chipBlue;
    private ConstraintLayout configMenu;
    private CheckBox mineLayer;
    private CheckBox othersLayer;
    private CheckBox trackLayer;
    private CheckBox teamLayer;
    private FloatingActionButton btnConfig;
    private FloatingActionButton btnRunStart;

    // running related
    Location startLocation;
    Location endLocation;
    String startTime;
    private Location lastKnownLocation;
    List<Marker> markers;
    List<LatLng> selfPoints;
    Polyline selfRoute;
    double distance;

    // others
    private Context context;
    private MapViewModel viewModel;
    public interface MapFragmentListener {
        void runStart();
        void runStop();
    }
    private MapFragmentListener mapFragmentListener;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        this.context = context;
        mapFragmentListener = (MapFragmentListener) context;
        // running params
        locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        // data observ
        startLocation = null;
        endLocation = startLocation;
        markers = new ArrayList<>();
        selfPoints = new ArrayList<>();
        Utils.setRequestingLocationUpdates(context, false);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_map, container, false);

        // instantiate the view model
        viewModel = ViewModelProviders.of(this).get(MapViewModel.class);

        // inflate the map fragment and start initialize the map
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        btnRunStart = root.findViewById(R.id.fab_run_mainPage);
        btnRunStart.setOnClickListener(new View.OnClickListener() {
            public void onClick (View view){
                if (!Utils.requestingLocationUpdates(context)) {
                    Snackbar snackbar = Snackbar.make(view, "Start Running...", Snackbar.LENGTH_LONG);
                    snackbar.setAction(R.string.dismiss_string, this);
                    snackbar.show();
                    mapFragmentListener.runStart();
                    Log.d("testo", "from here");
                    startRunning();
                } else {
                    Snackbar snackbar = Snackbar.make(view, "Stop Running...", Snackbar.LENGTH_LONG);
                    snackbar.setAction(R.string.dismiss_string, this);
                    snackbar.show();
                    Log.d("testo", "from here");
                    stopRunning();
                    mapFragmentListener.runStop();
                }
            }

        });

        configMenu = root.findViewById(R.id.mapMapConfig);
        configMenu.setVisibility(View.INVISIBLE);
        mineLayer = root.findViewById(R.id.cbMyLocation);
        mineLayer.setOnClickListener(e -> onMineToggled(e));
        mineLayer.setChecked(true);
        othersLayer = root.findViewById(R.id.cbMarker);
        othersLayer.setOnClickListener(e -> onOthersToggled(e));
        othersLayer.setChecked(true);
        trackLayer = root.findViewById(R.id.cbRoute);
        trackLayer.setOnClickListener(e -> onTrackToggled(e));
        trackLayer.setChecked(true);
        teamLayer = root.findViewById(R.id.cbTeam);
        teamLayer.setOnClickListener(e -> onTeamToggled(e));
        teamLayer.setChecked(true);
        btnConfig = root.findViewById(R.id.fabMapOptions);
        btnConfig.setOnClickListener(e -> onConfig(e));
        chipBlue = root.findViewById(R.id.chipBlue);
        chipRed = root.findViewById(R.id.chipRed);
        chipRed.setText("Step: 0");
        chipBlue.setText("Step: 0");

        return root;
    }

    /**
     * When map is ready, do the following actions:
     * 1. ask for permission
     * 2. update the map UI, add 'My locastion' control button on the map
     * 3. get current location and set it on the map
     * 4. bind the observer of view model
     */
    @Override
    public void onMapReady(final GoogleMap map) {
        googleMap = map;

        // Prompt the user for permission.
        if (!checkPermissions()) {
            requestPermissions();
        }

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation(DEFAULT_ZOOM);

        // bind the observer of view model here
        MapViewModel.getRunningParamMutableLiveData().observe(this, new Observer<RunningParam>() {
            @Override
            public void onChanged(RunningParam runningParam) {
                Log.d("testo", runningParam.toString());
                clearMarkers();
                if (runningParam.getState() == -1) return;
                else {
                    if (startLocation == null) {
                        startLocation = runningParam.getCurrLocation();
                        endLocation = runningParam.getCurrLocation();
                    }
                    distance += endLocation.distanceTo(runningParam.getCurrLocation());
                    endLocation = runningParam.getCurrLocation();
                    selfPoints.add(new LatLng(endLocation.getLatitude(), endLocation.getLongitude()));
                    chipRed.setText("Step: " + runningParam.getRed());
                    chipBlue.setText("Step: " + runningParam.getBlue());
                    List<Marker> tempMarkers = new ArrayList<>();
                    for (MarkerInfo markerInfo : runningParam.getMarkersInfo().values()) {
                        if (markerInfo.getState() == -1) continue;
                        MarkerOptions options = new MarkerOptions().position(markerInfo.getLatLng());
                        Bitmap bitmap = null;
                        if (markerInfo.getTeam().equals("red")) bitmap = createUserBitmapRed(markerInfo.getBitmap());
                        else bitmap = createUserBitmapBlue(markerInfo.getBitmap());
                        if (bitmap != null) {
                            options.title(CurrentUser.getInstance().getFriends().get(markerInfo.getEmail()).getName());
                            options.icon(BitmapDescriptorFactory.fromBitmap(bitmap)).snippet(markerInfo.getSteps() + "");
                        }
                        markers.add(googleMap.addMarker(options));
                    }
                    refreshView();
                }

            }
        });

        // if is running right now
        if (Utils.requestingLocationUpdates(context) && locationPermission) {
            Log.d("testo", "running zhene");
            startRunning();
        }
    }

    private void startRunning() {
        Log.d("testo","start");
        startTime = DateHelper.generateTimeStamp();
        getDeviceLocation(RUNNING_ZOOM);
    }

    private void stopRunning() {
        Log.d("testo","stop");
        onScreenshot();
    }

    private void initData() {
        startLocation = null;
        endLocation = startLocation;
        markers.clear();
        selfPoints.clear();
        chipRed.setText("Step: 0");
        chipBlue.setText("Step: 0");
        distance = 0;
        startTime = DateHelper.generateTimeStamp();
    }


    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void getDeviceLocation(int zoom) {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermission) {
                // get last known location
                lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastKnownLocation != null) {
                    // update it to the map
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()), zoom));
                } else {
                    // if not find the location, load a default one
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, zoom));
                    googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                }
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (googleMap == null) {
            return;
        }
        try {
            // if permission is already granted....
            if (locationPermission) {
                googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                googleMap.setMyLocationEnabled(false);
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
                requestPermissions();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    /**
     * Returns the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        locationPermission = PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return locationPermission;
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            Snackbar.make(
                    getActivity().findViewById(R.id.nav_view),
                    R.string.permission_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            requestPermissions(
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    MainActivity.REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    })
                    .show();
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MainActivity.REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == MainActivity.REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermission = true;
                if (Utils.requestingLocationUpdates(context)) {
                    Log.d("testo", "from there");
                    startRunning();
                }
                else
                    getDeviceLocation(DEFAULT_ZOOM);
            } else {
                // Permission denied.
                //setButtonsState(false);
                if (Utils.requestingLocationUpdates(context))
                    Log.d("testo", "from there");
                    stopRunning();
                Snackbar.make(
                        getActivity().findViewById(R.id.nav_view),
                        R.string.permission_denied_explanation,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        })
                        .show();
            }
            updateLocationUI();
        }
    }

    private Bitmap createUserBitmapRed(Bitmap loadBitmap) {
        Bitmap result = null;
        try {
            result = Bitmap.createBitmap(dp(62), dp(76), Bitmap.Config.ARGB_8888);
            result.eraseColor(Color.TRANSPARENT);
            Canvas canvas = new Canvas(result);
            Drawable drawable = getResources().getDrawable(R.drawable.livepin_red);
            drawable.setBounds(0, 0, dp(62), dp(76));
            drawable.draw(canvas);

            Paint roundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            RectF bitmapRect = new RectF();
            canvas.save();

            Bitmap bitmap = loadBitmap;
            //Bitmap bitmap = BitmapFactory.decodeFile(path.toString()); /*generate bitmap here if your image comes from any url*/
            if (bitmap != null) {
                BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                Matrix matrix = new Matrix();
                float scale = dp(52) / (float) bitmap.getWidth();
                matrix.postTranslate(dp(5), dp(5));
                matrix.postScale(scale, scale);
                roundPaint.setShader(shader);
                shader.setLocalMatrix(matrix);
                bitmapRect.set(dp(9), dp(9), dp(52+1), dp(52+1));
                canvas.drawRoundRect(bitmapRect, dp(26), dp(26), roundPaint);
            }
            canvas.restore();
            try {
                canvas.setBitmap(null);
            } catch (Exception e) {}
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return result;
    }

    private Bitmap createUserBitmapBlue(Bitmap loadBitMap) {
        Bitmap result = null;
        try {
            result = Bitmap.createBitmap(dp(62), dp(76), Bitmap.Config.ARGB_8888);
            result.eraseColor(Color.TRANSPARENT);
            Canvas canvas = new Canvas(result);
            Drawable drawable = getResources().getDrawable(R.drawable.livepin_blue);
            drawable.setBounds(0, 0, dp(62), dp(76));
            drawable.draw(canvas);

            Paint roundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            RectF bitmapRect = new RectF();
            canvas.save();

            Bitmap bitmap = loadBitMap;
            //Bitmap bitmap = BitmapFactory.decodeFile(path.toString()); /*generate bitmap here if your image comes from any url*/
            if (bitmap != null) {
                BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                Matrix matrix = new Matrix();
                float scale = dp(52) / (float) bitmap.getWidth();
                matrix.postTranslate(dp(5), dp(5));
                matrix.postScale(scale, scale);
                roundPaint.setShader(shader);
                shader.setLocalMatrix(matrix);
                bitmapRect.set(dp(9), dp(9), dp(52+1), dp(52+1));
                canvas.drawRoundRect(bitmapRect, dp(26), dp(26), roundPaint);
            }
            canvas.restore();
            try {
                canvas.setBitmap(null);
            } catch (Exception e) {}
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return result;
    }

    public int dp(float value) {
        if (value == 0) {
            return 0;
        }
        return (int) Math.ceil(getResources().getDisplayMetrics().density * value);
    }

    // ui private method

    private void showHideMarkers(boolean isShown) {
        if (isShown && markers != null) {
            for (Marker marker : markers) marker.setVisible(true);
        }
        else if (!isShown && markers != null)
            for (Marker marker : markers) marker.setVisible(false);
    }

    private void clearMarkers() {
        for (Marker marker : markers) {
            marker.remove();
        }
        markers.clear();
    }

    // polyline
    private void showHideRoute(boolean isShown) {
        if (isShown && selfRoute != null) selfRoute.setVisible(true);
        else if (!isShown && selfRoute != null) selfRoute.setVisible(false);
    }

    // team
    private void showHideTeam(boolean isShown) {
        if (isShown && chipRed != null && chipBlue != null) { chipRed.setVisibility(View.VISIBLE); chipBlue.setVisibility(View.VISIBLE);}
        else if (!isShown && chipRed != null && chipBlue != null) { chipRed.setVisibility(View.INVISIBLE); chipBlue.setVisibility(View.INVISIBLE);}
    }


    // option menu
    public void onConfig(View view) {
        if (configMenu.getVisibility() == View.INVISIBLE) configMenu.setVisibility(View.VISIBLE);
        else configMenu.setVisibility(View.INVISIBLE);
    }

    public void onMineToggled(View view) {
        googleMap.setMyLocationEnabled(mineLayer.isChecked());
    }

    public void onOthersToggled(View view) {
        refreshView();
    }

    public void onTrackToggled(View view) {
        refreshView();
    }

    public void onTeamToggled(View view) {
        refreshView();
    }

    private void refreshView() {
        showHideMarkers(othersLayer.isChecked());
        showHideRoute(trackLayer.isChecked());
        showHideTeam(teamLayer.isChecked());
    }

    public void onScreenshot() {
        googleMap.setMyLocationEnabled(false);
        showHideMarkers(false);
        showHideRoute(true);
        showHideTeam(false);
        Marker start = googleMap.addMarker(new MarkerOptions().position(new LatLng(startLocation.getLatitude(), startLocation.getLongitude())).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        Marker end = googleMap.addMarker(new MarkerOptions().position(new LatLng(endLocation.getLatitude(), endLocation.getLongitude())).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        recenterLocation(checkBounds(selfPoints), new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                snapShot();
                recenterLocation(endLocation, DEFAULT_ZOOM, new GoogleMap.CancelableCallback() {
                    @Override
                    public void onFinish() {
                        Log.d("testo", "snapshota");
                        start.remove();
                        end.remove();
                        googleMap.setMyLocationEnabled(true);
                        initData();
                        refreshView();
                        getDeviceLocation(DEFAULT_ZOOM);
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

    private void snapShot() {

        if (googleMap == null) return;

        final GoogleMap.SnapshotReadyCallback callback = new GoogleMap.SnapshotReadyCallback() {
            @Override
            public void onSnapshotReady(Bitmap snapshot) {
                // 2.1 Callback is called from the main thread, so we can modify the ImageView safely.
                int duration = DateHelper.getDurationDiff(startTime);
                float unitSpeed = (float)(distance*1.0  / duration == 0 ? 1 : distance*1.0  / duration);
                String speed = String.format("%.2f", 1000.0 / unitSpeed) + " min/km";
//                RunningRecord.runningRecords.add(new RunningRecord(startTime, DateHelper.generateDayInWeek(), distance + " m", speed, DateHelper.getDuration(duration), ImageHelper.RotateBitmap(snapshot, 90)));
                RunningRecord.runningRecords.add(new RunningRecord(startTime, DateHelper.generateDayInWeek(), distance + " m", speed, DateHelper.getDuration(duration), snapshot));
            }
        };
        googleMap.snapshot(callback);
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


}