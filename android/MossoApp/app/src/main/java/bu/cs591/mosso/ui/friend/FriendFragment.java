package bu.cs591.mosso.ui.friend;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import bu.cs591.mosso.ChatActivity;
import bu.cs591.mosso.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import bu.cs591.mosso.db.User;

public class FriendFragment extends Fragment {

    private FriendViewModel friendViewModel;

    private ListView lvFriends;

    private ListAdapter lvAdapter;

    private Context mContext;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        friendViewModel =
                ViewModelProviders.of(this).get(FriendViewModel.class);
        View root = inflater.inflate(R.layout.fragment_friend, container, false);
        mContext = getActivity();

        lvFriends = (ListView)root.findViewById(R.id.friendsListView);
        lvFriends.setAdapter(new FriendsListAdapter(mContext, new ArrayList<User>()));
        friendViewModel.getFriends().observe(this, new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                lvAdapter = new FriendsListAdapter(mContext, users);
                lvFriends.setAdapter(lvAdapter);
            }
        });

        return root;
    }
}

class FriendsListAdapter extends BaseAdapter {
    private List<User> friends;

    private Context context;

    public FriendsListAdapter(Context c, List<User> users) {
        context = c;
        friends = new ArrayList<>(users);
    }

    @Override
    public int getCount() {
        return friends.size();
    }

    @Override
    public Object getItem(int position) {
        return friends.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View row;

        if (convertView == null){  //indicates this is the first time we are creating this row.
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);  //Inflater's are awesome, they convert xml to Java Objects!
            row = inflater.inflate(R.layout.listitem_friends, parent, false);
        }
        else
        {
            row = convertView;
        }

        ImageView friendAvatar = (ImageView) row.findViewById(R.id.friendImageView);
        TextView friendName = (TextView) row.findViewById(R.id.friendNameTextView);
        TextView friendEmail = (TextView) row.findViewById(R.id.friendEmailTextView);
        User friend = friends.get(position);

        try {
            Picasso.get().load(friend.photoUrl.toString()).into(friendAvatar);
        } catch (Exception e) {
            friendAvatar.setImageResource(R.mipmap.ic_friend_avatar_round);
        }
        friendName.setText(friend.name);
        friendEmail.setText(friend.email);

        row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                User targetUser = friends.get(position);

                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra(ChatActivity.FRIEND_ID, targetUser.userID);
                context.startActivity(intent);
            }
        });

        return row;
    }
}