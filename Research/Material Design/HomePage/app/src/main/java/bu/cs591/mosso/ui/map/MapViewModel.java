package bu.cs591.mosso.ui.map;

import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import bu.cs591.mosso.db.MapMarker;

public class MapViewModel extends ViewModel {
    private List<MapMarker> markerList;

    private MutableLiveData<List<MapMarker>> neighbors;

    public MapViewModel() {
        markerList = new ArrayList<>();
        neighbors = new MutableLiveData<>();
        neighbors.setValue(markerList);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // add snapshot listener, continuing listen to the data....
        // when a change happens, onEvent will be called, update the LiveData
        // Since LiveData is updated, the UI will be automatically updated
        db.collection("testmarkers").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.e("MAPVIEWMODEL", "listen: error", e);
                    return;
                }
                Log.i("MAPVIEWMODEL", "update occur....");
                markerList = new ArrayList<>();
                for (QueryDocumentSnapshot dc : snapshots) {
                    MapMarker newMarker = new MapMarker(dc.getString("username"),
                            dc.getString("timestamp"),
                            dc.getDouble("latitude"),
                            dc.getDouble("longitude"));
                    markerList.add(newMarker);
                }
                neighbors.setValue(markerList);
            }
        });
        // make query once
//        db.collection("testmarkers").get()
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        if (task.isSuccessful()) {
//                            markerList = new ArrayList<>();
//                            for (QueryDocumentSnapshot document : task.getResult()) {
//                                Map<String, Object> data = document.getData();
//                                String username = (String)data.get("username");
//                                String timestamp = (String)data.get("timestamp");
//                                Double latitude = (Double)data.get("latitude");
//                                Double longitude = (Double)data.get("longitude");
//                                markerList.add(new MapMarker(username, timestamp, latitude, longitude));
//                            }
//                            neighbors.setValue(markerList);
//                        } else {
//                            Log.w("MAPVIEWMODEL", "Error getting documents.", task.getException());
//                        }
//                    }
//                });
    }

    public LiveData<List<MapMarker>> getNeighbors() {
        return neighbors;
    }
}
