package bu.cs591.mosso.db;

import android.location.Location;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class RunningRepo {
    private static final RunningRepo INSTANCE = new RunningRepo();
    private static final float UPDATE_DIST_LIMIT = 10f;

    private Location currLocation;
    private List<Location> locations;
    private MutableLiveData<List<Location>> routes;

    private RunningRepo() {
        currLocation = null;
        locations = new ArrayList<>();
        routes = new MutableLiveData<>();
    }

    public static RunningRepo getInstance() {
        return INSTANCE;
    }

    public LiveData<List<Location>> getRoutes() {
        return routes;
    }

    public void addNewLocation(Location location) {
        if (currLocation == null || currLocation.distanceTo(location) > UPDATE_DIST_LIMIT) {
            currLocation = location;
            locations.add(currLocation);
            routes.setValue(locations);
        }
    }
}

