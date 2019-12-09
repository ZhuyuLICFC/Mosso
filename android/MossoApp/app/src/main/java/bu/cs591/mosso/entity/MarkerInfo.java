package bu.cs591.mosso.entity;

import android.graphics.Bitmap;

import com.google.android.gms.maps.model.LatLng;

public class MarkerInfo {

    private String email;
    private LatLng latLng;
    private int steps;
    private String team;
    private int state;
    private Bitmap bitmap;

    public MarkerInfo(String email, LatLng latLng, int steps, String team, int state, Bitmap bitmap) {
        this.email = email;
        this.latLng = latLng;
        this.steps = steps;
        this.team = team;
        this.state = state;
        this.bitmap = bitmap;
    }

    public String getEmail() {
        return email;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public int getSteps() {
        return steps;
    }

    public String getTeam() {
        return team;
    }

    public int getState() {
        return state;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public void setState(int state) {
        this.state = state;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
