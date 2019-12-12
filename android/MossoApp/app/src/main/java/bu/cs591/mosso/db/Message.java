package bu.cs591.mosso.db;

import java.util.Date;

/**
 *  Data Structure for message in chat room.
 */
public class Message {
    // the content of this message
    private String text;
    // the name of the sender
    private String name;
    // the photo url of the sender
    private String photoUrl;
    // the image url in the message
    private String imageUrl;
    // the room id of this message
    private String room;
    // the created time
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
