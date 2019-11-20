package bu.cs591.mosso.ui.map;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import bu.cs591.mosso.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import bu.cs591.mosso.db.MapMarker;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private FloatingActionButton btnRunStart;

    private Context mContext;

    private MapViewModel mViewModel;

    // Map Related...
    private static final String TAG = "MAPS_DEBUG";//MapsActivity.class.getSimpleName();
    private GoogleMap mMap;
    private CameraPosition mCameraPosition;

    private LocationManager locationManager;

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;

    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    private List<Marker> markers;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mContext = context;
        locationManager = (LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_map, container, false);

        // instantiate the view model
        mViewModel = ViewModelProviders.of(this).get(MapViewModel.class);

        markers = new ArrayList<>();

        // inflate the map fragment and start initialize the map
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // button just to show the snackbar...
        btnRunStart = root.findViewById(R.id.fab_run_mainPage);
        btnRunStart.setOnClickListener(new View.OnClickListener() {
            public void onClick (View view){
                Snackbar snackbar = Snackbar.make(view , "Start Running...", Snackbar.LENGTH_LONG);
                snackbar.setAction(R.string.undo_string, this);
                snackbar.show();
            }

        });
        FloatingActionButton textView = root.findViewById(R.id.fab_snapBar_mainPage);
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
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

        // bind the observer of view model here
        mViewModel.getNeighbors().observe(this, new Observer<List<MapMarker>>() {
            @Override
            public void onChanged(List<MapMarker> mapMarkers) {
                // clear all the previous markers on the map
                clearMarkers();
                // iterate every new marker and add it to the map
                for (MapMarker marker : mapMarkers) {
                    markers.add(mMap.addMarker(new MarkerOptions()
                        .position(marker.getLatLng())
                        .title(marker.getUsername())
                        .snippet(marker.getTimestamp())
                        .visible(true)));
                }
            }
        });
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
    private void getDeviceLocation() {
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
                            new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                } else {
                    // if not find the location, load a default one
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                    mMap.getUiSettings().setMyLocationButtonEnabled(false);
                }
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    /**
     * Prompts the user for permission to use the device location.
     */
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(mContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                    getDeviceLocation();
                }
            }
        }
        updateLocationUI();
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
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Mosso options");
        getActivity().getMenuInflater().inflate(R.menu.example_layer_menu, menu);

        // use switch if you have more menus by call v

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
}