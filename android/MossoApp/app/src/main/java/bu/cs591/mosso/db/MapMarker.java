package bu.cs591.mosso.db;

import com.google.android.gms.maps.model.LatLng;

public class MapMarker {
    private String username;

    private String timestamp;

    private LatLng latLng;

    public MapMarker(String name, String time, double latitude, double longitude) {
        username = name;
        timestamp = time;
        latLng = new LatLng(latitude, longitude);
    }

    public String getUsername() { return username; }

    public String getTimestamp() { return timestamp; }

    public LatLng getLatLng() { return latLng; }
}
