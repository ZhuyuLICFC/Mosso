package bu.cs591.mosso.db;

import java.util.Date;

public class Message {
    private String text;
    private String name;
    private String photoUrl;
    private String imageUrl;
    private String room;
    private Date timestamp;

    public Message() {
    }

    public Message(String text, String name, String photoUrl, String room, String imageUrl) {
        this.text = text;
        this.name = name;
        this.room = room;
        this.photoUrl = photoUrl;
        this.imageUrl = imageUrl;
    }

    public Date getTimestamp() { return this.timestamp; }

    public void setTimestamp(Date time) { this.timestamp = time; }

    public void setRoom(String room) { this.room = room; }

    public String getRoom() { return this.room; }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getText() {
        return text;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
