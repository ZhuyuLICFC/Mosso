package bu.cs591.mosso.ui.account;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.data.model.Resource;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import bu.cs591.mosso.LogInActivity;
import bu.cs591.mosso.R;
import bu.cs591.mosso.db.User;
import bu.cs591.mosso.entity.CurrentUser;

import static com.firebase.ui.auth.AuthUI.TAG;

public class AccountFragment extends Fragment {

    private AccountViewModel accountViewModel;
    private Button btnSignOut;
    private TextView txtEmail;
    private ImageView photo;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        accountViewModel =
                ViewModelProviders.of(this).get(AccountViewModel.class);
        final View root = inflater.inflate(R.layout.fragment_account, container, false);
        final TextView textView = root.findViewById(R.id.text_account);
        accountViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        txtEmail = (TextView) root.findViewById(R.id.txtEmail);
        photo = (ImageView) root.findViewById(R.id.photo);

        accountViewModel.getCurrentAccount().observe(this, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                txtEmail.setText(user.email);
                textView.setText(user.name);
                Picasso.get().load(user.photoUrl.toString()).into(photo);
            }
        });

        btnSignOut = (Button)root.findViewById(R.id.btnLogOut);
        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AuthUI.getInstance()
                        .signOut(getContext())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                Intent intent = new Intent(getActivity(), LogInActivity.class);
                                CurrentUser.setInstance();
                                startActivity(intent);
                            }
                        });
            }
        });

        return root;
    }
}