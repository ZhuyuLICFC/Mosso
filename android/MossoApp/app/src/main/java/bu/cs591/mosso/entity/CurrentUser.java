package bu.cs591.mosso.entity;

import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

public class CurrentUser {

    private static CurrentUser instance;

    private String id;
    private String name;
    private String email;
    private Uri photoUrl;
    private List<String> friends;

    private CurrentUser(String id, String n, String mail, Uri photo, List<String> friends) {
        this.id = id;
        name = n;
        email = mail;
        photoUrl = photo;
        this.friends = friends;
    }

    public static CurrentUser getInstance() {
        return instance;
    }

    public static void setInstance(String id, String name, String mail, Uri photoUrl, List<String> friends) {
        instance = new CurrentUser(id, name, mail, photoUrl, friends);
    }

    public static void setInstance() {
        instance = null;
    }

    @Override
    public String toString() {
        return "CurrentUser{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", photoUrl=" + photoUrl +
                ", friends=" + friends +
                '}';
    }
}
