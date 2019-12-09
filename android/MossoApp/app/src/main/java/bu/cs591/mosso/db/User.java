package bu.cs591.mosso.db;

import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

public class User {
    public String userID;
    public String name;
    public String email;
    public Uri photoUrl;

    public User(String n, String mail, Uri photo) {
        name = n;
        email = mail;
        photoUrl = photo;
        userID = "";
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
