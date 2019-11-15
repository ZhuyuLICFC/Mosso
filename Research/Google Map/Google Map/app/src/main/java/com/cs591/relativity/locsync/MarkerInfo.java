package com.cs591.relativity.locsync;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

// model layer entity class about google map marker
public class MarkerInfo {

    public static List<MarkerInfo> markerInfos = new ArrayList();

    private LatLng latLng;
    private String title;
    private String info;
    private BitmapDescriptor color;

    public MarkerInfo(LatLng latLng, String title, String info) {
        this.latLng = latLng;
        this.title = title;
        this.info = info;
        this.color = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
    }

    public MarkerInfo(LatLng latLng, String title, String info, BitmapDescriptor bitmapDescriptor) {
        this.latLng = latLng;
        this.title = title;
        this.info = info;
        this.color = bitmapDescriptor;
    }

    public static List<MarkerInfo> getMakerInfos() {
        return markerInfos;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public String getTitle() {
        return title;
    }

    public String getInfo() {
        return info;
    }

    public BitmapDescriptor getColor() {
        return color;
    }
}
