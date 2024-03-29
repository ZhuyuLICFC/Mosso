package bu.cs591.mosso.entity;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import androidx.lifecycle.MutableLiveData;
import bu.cs591.mosso.FitData;

public class RunningParam {

    private static RunningParam instance;

    private Location currLocation;
    private Location startLocation;
    private Location endLocation;
    private String startTime;
    private int selfCurSteps;
    private int selfPrevSteps;
    private Map<String, MarkerInfo> markersInfo;
    private int state;
    private int red;
    private int blue;
    private List<LatLng> selfPoints;
    double distance = 0;

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    private RunningParam(Location currLocation, Map<String, MarkerInfo> markersInfo) {
        this.currLocation = currLocation;
        this.markersInfo = markersInfo;
        this.selfCurSteps = FitData.getFitStep();
        this.selfPrevSteps = this.selfCurSteps;
        red = 0;
        blue = 0;
        this.state = -1;
        selfPoints = new ArrayList<>();
    }

    public static RunningParam getInstance() {
        return instance;
    }

    public static RunningParam getInstance(Location currLocation, Map<String, MarkerInfo> markersInfo) {
        instance = new RunningParam(currLocation, markersInfo);
        return instance;
    }

    public static void setInstance() {
        RunningParam.instance = null;
    }

    public static void setInstance(Location currLocation, Map<String, MarkerInfo> markersInfo) {
        RunningParam.instance = new RunningParam(currLocation, markersInfo);
    }

    public Location getCurrLocation() {
        return currLocation;
    }

    public void setCurrLocation(Location currLocation) {
        this.currLocation = currLocation;
    }


    public Map<String, MarkerInfo> getMarkersInfo() {
        return markersInfo;
    }

    public void setMarkersInfo(Map<String, MarkerInfo> markersInfo) {
        this.markersInfo = markersInfo;
    }

    public int getSelfCurSteps() {
        return selfCurSteps;
    }

    public int getSelfPrevSteps() {
        return selfPrevSteps;
    }

    public void setSelfCurSteps(int selfCurSteps) {
        this.selfCurSteps = selfCurSteps;
    }

    public void setSelfPrevSteps(int selfPrevSteps) {
        this.selfPrevSteps = selfPrevSteps;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getRed() {
        return red;
    }

    public void setRed(int red) {
        this.red = red;
    }

    public int getBlue() {
        return blue;
    }

    public void setBlue(int blue) {
        this.blue = blue;
    }

    public List<LatLng> getSelfPoints() {
        return selfPoints;
    }

    @Override
    public String toString() {
        return "RunningParam{" +
                "currLocation=" + currLocation +
                ", selfCurSteps=" + selfCurSteps +
                ", selfPrevSteps=" + selfPrevSteps +
                ", markersInfo=" + markersInfo +
                ", state=" + state +
                ", red=" + red +
                ", blue=" + blue +
                '}';
    }

    public Location getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(Location startLocation) {
        this.startLocation = startLocation;
    }

    public Location getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(Location endLocation) {
        this.endLocation = endLocation;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }
}
