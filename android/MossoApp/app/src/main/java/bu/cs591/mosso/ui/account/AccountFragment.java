package bu.cs591.mosso.ui.account;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.w3c.dom.Text;

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

public class AccountFragment extends Fragment {

    private AccountViewModel accountViewModel;
    private Button btnSignOut;
    private TextView txtName;
    private TextView txtEmail;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        accountViewModel =
                ViewModelProviders.of(this).get(AccountViewModel.class);
        View root = inflater.inflate(R.layout.fragment_account, container, false);
        final TextView textView = root.findViewById(R.id.text_account);
        accountViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        txtEmail = (TextView) root.findViewById(R.id.txtEmail);
        txtName = (TextView) root.findViewById(R.id.txtName);

        accountViewModel.getCurrentAccount().observe(this, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                txtName.setText(user.name);
                txtEmail.setText(user.email);
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
                                startActivity(intent);
                            }
                        });
            }
        });

        return root;
    }
}