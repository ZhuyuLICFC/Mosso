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
import bu.cs591.mosso.db.User;
import bu.cs591.mosso.entity.RunningParam;

public class MapViewModel extends ViewModel {

    private static MutableLiveData<RunningParam> runningParamMutableLiveData = new MutableLiveData<>();

    public MapViewModel() {

    }

    public static MutableLiveData<RunningParam> getRunningParamMutableLiveData() {
        return runningParamMutableLiveData;
    }
}
