package bu.cs591.mosso.ui.friend;

import android.net.Uri;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import bu.cs591.mosso.db.User;

/**
 * the view model for friend fragment
 */
public class FriendViewModel extends ViewModel {

    private List<User> friends;

    private MutableLiveData<List<User>> mFriends;

    private FirebaseFirestore db;

    public FriendViewModel() {
        friends = Collections.synchronizedList(new ArrayList<User>());
        mFriends = new MutableLiveData<>();
        db = FirebaseFirestore.getInstance();
        mFriends.setValue(friends);

        FirebaseUser fireUser = FirebaseAuth.getInstance().getCurrentUser();
        if (fireUser != null)
            loadFriends(fireUser.getUid());
    }

    /**
     * load all the friends from database based on current user
     * @param uid
     */
    private void loadFriends(String uid) {
        db.collection("friends").document(uid)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot doc, @Nullable FirebaseFirestoreException e) {
                        friends.clear();
                        Map<String, Object> docMap = doc.getData();
                        if (docMap != null) {
                            for (String uid : docMap.keySet()) {
                                db.collection("users").document(uid)
                                        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot doc = task.getResult();
                                            if (doc.exists()) {
                                                User user = new User(doc.getString("name"),
                                                        doc.getString("email"),
                                                        Uri.parse(doc.getString("photo")));
                                                user.setUserID(doc.getId());
                                                friends.add(user);
                                                mFriends.setValue(friends);
                                            }
                                        } else {
                                            Log.w("ACCOUNT_DEBUG", "Error getting documents.", task.getException());
                                            //addUser();
                                        }
                                    }
                                });
                            }
                        }
                    }
                });
    }

    public LiveData<List<User>> getFriends() { return mFriends; }
}