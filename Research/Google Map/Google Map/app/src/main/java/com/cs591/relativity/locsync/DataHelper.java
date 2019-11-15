package com.cs591.relativity.locsync;

import android.location.Location;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DataHelper {


    // generate two markers with corresponding user info
    public static void generateMarkers(Location location) {
        MarkerInfo.markerInfos.add(new MarkerInfo(new LatLng(location.getLatitude() + 0.0001, location.getLongitude() + 0.0001), "Gappery", "Step: 270", BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        MarkerInfo.markerInfos.add(new MarkerInfo(new LatLng(location.getLatitude() - 0.00015, location.getLongitude() - 0.00015), "Lian", "Step: 100000", BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
    }

    // randomly generate polyline to simulate user moving track
    public static List<LatLng> generatePolyLines(Location location, int num) {
        Random random = new Random();
        List<LatLng> points = new ArrayList();
        for (int i = 0; i < num; i++) {
            points.add(new LatLng(location.getLatitude() + (random.nextInt(20) - 10) * 0.0001, location.getLongitude() + (random.nextInt(20) - 10) * 0.0001));
        }
        return points;
    }

}
