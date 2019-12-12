package bu.cs591.mosso.db;

import com.google.android.gms.maps.model.LatLng;

/**
 *  Data Structure for markers on the map.
**/
public class MapMarker {

    // user name
    private String username;

    // created time
    private String timestamp;

    // location
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
