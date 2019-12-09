package bu.cs591.mosso.entity;

import android.net.Uri;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CurrentUser {

    private static CurrentUser instance;

    private String id;
    private String name;
    private String email;
    private Uri photoUrl;
    private Map<String, BasicUser> friends;
    private String team;

    public CurrentUser(String id, String name, String email, Uri photoUrl, Map<String, BasicUser> friends, String team) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.photoUrl = photoUrl;
        this.friends = friends;
        this.team = team;
    }

    public static CurrentUser getInstance() {
        return instance;
    }

    public static void setInstance(String id, String name, String mail, Uri photoUrl, Map<String, BasicUser> friends, String team) {
        instance = new CurrentUser(id, name, mail, photoUrl, friends, team);
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
                ", team='" + team + '\'' +
                '}';
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Uri getPhotoUrl() {
        return photoUrl;
    }

    public Map<String, BasicUser> getFriends() {
        return friends;
    }

    public String getTeam() {
        return team;
    }
}
