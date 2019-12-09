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
import android.provider.Settings;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import bu.cs591.mosso.BuildConfig;
import bu.cs591.mosso.MainActivity;
import bu.cs591.mosso.R;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import bu.cs591.mosso.Utils;
import bu.cs591.mosso.db.MapMarker;
import bu.cs591.mosso.db.RunningRepo;

public class MapFragment extends Fragment implements OnMapReadyCallback{

    private FloatingActionButton btnRunStart;

    private Context mContext;

    private MapViewModel mViewModel;
    private RunningRepo myRepo;

    public interface MapFragmentListener {
        public void runStart();
        public void runStop();
    }

    private MapFragmentListener mListener;

    // Map Related...
    private static final String TAG = MapFragment.class.getSimpleName();
    private GoogleMap mMap;
    private CameraPosition mCameraPosition;

    private LocationManager locationManager;

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;
    private static final int RUNNING_ZOOM = 18;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;

    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    private List<Marker> markers;

    private Polyline routes;

    private Observer<List<Location>> locationObserver;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mContext = context;
        mListener = (MapFragmentListener) context;
        markers = new ArrayList<>();
        myRepo = RunningRepo.getInstance();

        locationManager = (LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE);
        locationObserver = new Observer<List<Location>>() {
            @Override
            public void onChanged(List<Location> locations) {
                List<LatLng> points = new LinkedList<>();
                for (Location loc : locations) {
                    points.add(new LatLng(loc.getLatitude(), loc.getLongitude()));
                }
                routes.setPoints(points);
            }
        };
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_map, container, false);

        // instantiate the view model
        mViewModel = ViewModelProviders.of(this).get(MapViewModel.class);

        // inflate the map fragment and start initialize the map
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btnRunStart = root.findViewById(R.id.fab_run_mainPage);
        btnRunStart.setOnClickListener(new View.OnClickListener() {
            public void onClick (View view){
                if (!Utils.requestingLocationUpdates(mContext)) {
                    Snackbar snackbar = Snackbar.make(view, "Start Running...", Snackbar.LENGTH_LONG);
                    snackbar.setAction(R.string.dismiss_string, this);
                    snackbar.show();
                    mListener.runStart();
                    startRunning();
                } else {
                    Snackbar snackbar = Snackbar.make(view, "Stop Running...", Snackbar.LENGTH_LONG);
                    snackbar.setAction(R.string.dismiss_string, this);
                    snackbar.show();
                    stopRunning();
                    mListener.runStop();
                }
            }

        });
        FloatingActionButton textView = root.findViewById(R.id.fabMapOptions);
        registerForContextMenu(textView);

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
        mMap = map;

        // Prompt the user for permission.
        if (!checkPermissions()) {
            requestPermissions();
        }

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation(DEFAULT_ZOOM);

        // bind the observer of view model here
        mViewModel.getNeighbors().observe(this, new Observer<List<MapMarker>>() {
            @Override
            public void onChanged(List<MapMarker> mapMarkers) {
                // clear all the previous markers on the map
                clearMarkers();
                // iterate every new marker and add it to the map
                for (MapMarker marker : mapMarkers) {
                    LatLng latLng = new LatLng(marker.getLatLng().latitude, marker.getLatLng().longitude);
                    MarkerOptions options = new MarkerOptions().position(latLng);
                    Bitmap bitmap = createUserBitmapRed();
                    //if team Blue, Bitmap bitmap = createUserBitmapBlue();
                    if(bitmap!=null){
                        options.title(marker.getUsername());
                        options.icon(BitmapDescriptorFactory.fromBitmap(bitmap)).snippet(marker.getTimestamp());
                        markers.add(mMap.addMarker(options));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
                    }
                }

            }
        });

        // if is running right now
        if (Utils.requestingLocationUpdates(mContext) && mLocationPermissionGranted) {
            startRunning();
        }
    }

    private void startRunning() {
        routes = mMap.addPolyline(new PolylineOptions().color(Color.RED));
        myRepo.getRoutes().observe(this, locationObserver);
        getDeviceLocation(RUNNING_ZOOM);
    }

    private void stopRunning() {
        myRepo.getRoutes().removeObserver(locationObserver);
        routes.remove();
        getDeviceLocation(DEFAULT_ZOOM);
    }

    private void clearMarkers() {
        for (Marker marker : markers) {
            marker.remove();
        }
        markers.clear();
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
            if (mLocationPermissionGranted) {
                // get last known location
                mLastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (mLastKnownLocation != null) {
                    // update it to the map
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), zoom));
                } else {
                    // if not find the location, load a default one
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, zoom));
                    mMap.getUiSettings().setMyLocationButtonEnabled(false);
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
        if (mMap == null) {
            return;
        }
        try {
            // if permission is already granted....
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
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
        mLocationPermissionGranted = PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(mContext,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return mLocationPermissionGranted;
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
                mLocationPermissionGranted = true;
                if (Utils.requestingLocationUpdates(mContext))
                    startRunning();
                else
                    getDeviceLocation(DEFAULT_ZOOM);
            } else {
                // Permission denied.
                //setButtonsState(false);
                if (Utils.requestingLocationUpdates(mContext))
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

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Mosso options");
        getActivity().getMenuInflater().inflate(R.menu.example_layer_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {


        switch (item.getItemId()){
            case R.id.option_myLocation_1:
                Toast.makeText(getActivity(), "My location seletcted", Toast.LENGTH_SHORT).show();
                int  a = item.getItemId();

                return true;
            case R.id.option_track_2:
                Toast.makeText(getActivity(),"Track selected ", Toast.LENGTH_SHORT ).show();
                return true;

            case R.id.option_other_Location_3:
                Toast.makeText(getActivity(),"Other Location selected", Toast.LENGTH_SHORT).show();
                return true;

            default:
                return super.onContextItemSelected(item);
        }

    }

    private Bitmap createUserBitmapRed() {
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

            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.avatar);
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

    private Bitmap createUserBitmapBlue() {
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

            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.avatar);
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
}