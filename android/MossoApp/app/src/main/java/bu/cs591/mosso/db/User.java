package bu.cs591.mosso.db;

import android.net.Uri;

public class User {
    public String name;
    public String email;
    public Uri photoUrl;

    public User(String n, String mail, Uri photo) {
        name = n;
        email = mail;
        photoUrl = photo;
    }
}
