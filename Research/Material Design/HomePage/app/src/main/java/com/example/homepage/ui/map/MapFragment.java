package com.example.homepage.ui.map;

import android.app.Activity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.homepage.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    FloatingActionButton textbtn;
    GoogleMap googleMap;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_map, container, false);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        textbtn = root.findViewById(R.id.fab_run_mainPage);
        textbtn.setOnClickListener(new View.OnClickListener() {
            public void onClick (View view){
                Snackbar snackbar = Snackbar.make(view , "This is Mosso", Snackbar.LENGTH_LONG);
                snackbar.setAction(R.string.undo_string, this);
                snackbar.show();
            }

        });
        FloatingActionButton textView = root.findViewById(R.id.fab_snapBar_mainPage);
        registerForContextMenu(textView);
        return root;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(42.3505, -71.1054), 19);
        googleMap.animateCamera(cameraUpdate);
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