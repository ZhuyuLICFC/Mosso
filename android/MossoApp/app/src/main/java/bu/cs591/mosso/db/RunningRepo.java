package bu.cs591.mosso.db;

import android.location.Location;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * Running locations repository for location tracking
 * It's a singleton class
 */
public class RunningRepo {
    private static final RunningRepo INSTANCE = new RunningRepo();
    // the minimum distance between current location and last updated location
    private static final float UPDATE_DIST_LIMIT = 10f;

    // current location
    private Location currLocation;
    // a list of locations, record the route
    private List<Location> locations;
    // a list of locations, encapsulated by LiveData
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

    /**
     * add a new location to the repo
     * @param location: new location
     */
    public void addNewLocation(Location location) {
        // if the distance between newlocation and last updated location is larger than the
        // upper limit, we will put this new location into the repo and update routes
        if (currLocation == null || currLocation.distanceTo(location) > UPDATE_DIST_LIMIT) {
            currLocation = location;
            locations.add(currLocation);
            routes.setValue(locations);
        }
    }
}

