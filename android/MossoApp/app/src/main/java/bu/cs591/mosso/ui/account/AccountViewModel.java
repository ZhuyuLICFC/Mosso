package bu.cs591.mosso.ui.account;

import android.net.Uri;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import bu.cs591.mosso.db.User;

public class AccountViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    private MutableLiveData<User> myAccount;

    private User mUser;

    public AccountViewModel() {
        mText = new MutableLiveData<>();

        myAccount = new MutableLiveData<>();
        FirebaseUser fireUser = FirebaseAuth.getInstance().getCurrentUser();
        if (fireUser == null) {
            Log.e("ACCOUNT_DEBUG", "fireUser is null?");
        } else {
            Log.i("ACCOUNT_DEBUG", fireUser.getUid());
            Log.i("ACCOUNT_DEBUG", fireUser.getEmail());
        }
        mUser = new User(fireUser.getDisplayName(),
                fireUser.getEmail(), fireUser.getPhotoUrl());
        myAccount.setValue(mUser);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // make query once
        db.collection("users").document(fireUser.getUid())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        mUser = new User(doc.getString("name"),
                                doc.getString("email"),
                                Uri.parse(doc.getString("photo")));
                        myAccount.setValue(mUser);
                    } else {
                        addUser();
                    }
                } else {
                    Log.w("ACCOUNT_DEBUG", "Error getting documents.", task.getException());
                    Log.d("testo","failed");
                    //addUser();
                }
            }
        });
    }

    private void addUser() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", mUser.name);
        data.put("email", mUser.email);
        data.put("photo", mUser.photoUrl.toString());

        FirebaseUser fireUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(fireUser.getUid()).set(data);
    }

    public LiveData<String> getText() {
        return mText;
    }

    public LiveData<User> getCurrentAccount() { return myAccount; }
}