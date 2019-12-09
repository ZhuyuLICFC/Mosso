package bu.cs591.mosso.entity;

import android.graphics.Bitmap;

public class BasicUser {

    String email;
    String id;
    String name;
    String team;
    Bitmap bitmap;

    public BasicUser(String email, String id, String name, String team, Bitmap bitmap) {
        this.email = email;
        this.id = id;
        this.name = name;
        this.team = team;
        this.bitmap = bitmap;
    }

    public String getEmail() {
        return email;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTeam() {
        return team;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }
}
