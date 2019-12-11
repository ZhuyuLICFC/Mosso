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

/**
 * Friend Fragment
 * present a friends list, user can view all its friends here
 * also, user can go to the chat room by clicking the friends here.
 */
public class FriendFragment extends Fragment {

    // friend view model, connected to the database
    private FriendViewModel friendViewModel;

    // use a list view to present all the friends.
    private ListView lvFriends;

    // the adapter for the list view
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
                // set the adapter, force the list view re-render the friends list.
                lvFriends.setAdapter(lvAdapter);
            }
        });

        return root;
    }
}

/**
 * FriendsListAdapter
 * the adapter for the list view
 */
class FriendsListAdapter extends BaseAdapter {
    private List<User> friends;

    private Context context;

    public FriendsListAdapter(Context c, List<User> users) {
        context = c;
        friends = new ArrayList<>(users);
    }

    /**
     * get the number of items in the list view.
     * @return
     */
    @Override
    public int getCount() {
        return friends.size();
    }

    /**
     * get the list items
     * @param position
     * @return
     */
    @Override
    public Object getItem(int position) {
        return friends.get(position);
    }

    /**
     * get item id
     * @param position
     * @return
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * get the view of each list item
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
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

        // update the UI
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

        // also add a onClickListener to current row, thus user can move to the chat room
        // by clicking this row
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