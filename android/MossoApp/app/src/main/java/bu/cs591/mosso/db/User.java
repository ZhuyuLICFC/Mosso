package bu.cs591.mosso.db;

import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

/**
 * User data structure
 */
public class User {
    // user ID
    public String userID;
    // user name
    public String name;
    // user's email
    public String email;
    // the photo url of the user
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
