package bu.cs591.mosso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.core.Query;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import bu.cs591.mosso.R;
import bu.cs591.mosso.db.User;
import bu.cs591.mosso.entity.BasicUser;
import bu.cs591.mosso.entity.CurrentUser;

/**
 * Log in Activity
 * use firebase authentication to log in
 */
public class LogInActivity extends AppCompatActivity {

    public static AtomicInteger cnt = new AtomicInteger(0);
    public static int total = -1;
    private Button btnSignIn;
    private static final int RC_SIGN_IN = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            login();
        }

        btnSignIn = (Button) findViewById(R.id.btnSignIn);
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // create the sign-in providers
                List<AuthUI.IdpConfig> providers = Arrays.asList(
                        new AuthUI.IdpConfig.EmailBuilder().build(),
                        new AuthUI.IdpConfig.GoogleBuilder().build());

                // create and load sing-in intent
                startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setAvailableProviders(providers)
                                .build(),
                        RC_SIGN_IN);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                login();
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
                String err = response.getError().getMessage();
                Toast.makeText(this, "Sign in falied: " + err, Toast.LENGTH_SHORT).show();
            }
        }
    }

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    // make query once

    private void login() {
        prepareUser();
    }

    private void prepareUser() {

        ProgressDialog progressDialog = ProgressDialog.show(LogInActivity.this, "",
                "Preparing Data. Please wait...", true);
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final Map<String, BasicUser> friendsInfo = new HashMap<>();
        db.collection("users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                Log.d("testo", task.isSuccessful() + "");
                if (task.isSuccessful()) {
                    total = task.getResult().size();
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        if (queryDocumentSnapshot.getString("email").equals(firebaseUser.getEmail())) continue;
                        else {
                            Picasso.get().load(queryDocumentSnapshot.getString(("photo"))).into(new Target() {
                                @Override
                                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                    Log.d("testo", "dive in");
                                    friendsInfo.put(queryDocumentSnapshot.getString( "email"), new BasicUser(queryDocumentSnapshot.getString("email"),
                                            queryDocumentSnapshot.getId(),
                                            queryDocumentSnapshot.getString("name"),
                                            queryDocumentSnapshot.getString("team"),
                                            bitmap));
                                    LogInActivity.cnt.incrementAndGet();
                                    if (cnt.get() == total - 1) {
                                        progressDialog.dismiss();
                                        Intent intent = new Intent(LogInActivity.this, MainActivity.class);
                                        startActivity(intent);
                                    }

                                }

                                @Override
                                public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                                }

                                @Override
                                public void onPrepareLoad(Drawable placeHolderDrawable) {

                                }
                            });
                        }
                    }
                } else {
                }
            }
        });

        db.collection("users").document(firebaseUser.getUid())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                Log.d("testo", task.isSuccessful() + "");
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        CurrentUser.setInstance(firebaseUser.getUid(), doc.getString("name"), firebaseUser.getEmail(), Uri.parse(doc.getString("photo")), friendsInfo, doc.getString("team"));
                    }
                    Log.w("ACCOUNT_DEBUG", "Error getting documents.", task.getException());
                }
            }
        });
    }

}
