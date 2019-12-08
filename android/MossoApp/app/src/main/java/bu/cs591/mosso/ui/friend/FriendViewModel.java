package bu.cs591.mosso.ui.friend;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import bu.cs591.mosso.db.User;

public class FriendViewModel extends ViewModel {

    private List<User> friends;

    private MutableLiveData<String> mText;

    public FriendViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is friend fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}