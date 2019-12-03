package bu.cs591.mosso.ui.friend;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import bu.cs591.mosso.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

public class FriendFragment extends Fragment {

    private FriendViewModel friendViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        friendViewModel =
                ViewModelProviders.of(this).get(FriendViewModel.class);
        View root = inflater.inflate(R.layout.fragment_friend, container, false);

        return root;
    }
}