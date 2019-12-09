package bu.cs591.mosso.ui.history;

import android.net.Uri;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import bu.cs591.mosso.db.User;
import bu.cs591.mosso.entity.RunningRecord;

public class HistoryViewModel extends ViewModel {

    private MutableLiveData<List<RunningRecord>> liveUsers;
    private List<RunningRecord> runningRecords;

    public HistoryViewModel() {
        liveUsers = new MutableLiveData<>();
        runningRecords = new ArrayList<>();

        FirebaseUser fireUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // make query once
        db.collection("historys")
                .document("zhuyuli@bu.edu")
                .collection("running")
                .orderBy("date", Query.Direction.DESCENDING)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        runningRecords.add(new RunningRecord(queryDocumentSnapshot.get("date") + "", queryDocumentSnapshot.getString("weekNo"), queryDocumentSnapshot.getString("distance"), queryDocumentSnapshot.getString("speed"), queryDocumentSnapshot.getString("duration")));
                    }
                    liveUsers.setValue(runningRecords);
                } else {
                    Log.w("ACCOUNT_DEBUG", "Error getting documents.", task.getException());
                    //addUser();
                }
            }
        });
    }

    public MutableLiveData<List<RunningRecord>> getLiveUsers() {
        return liveUsers;
    }
}