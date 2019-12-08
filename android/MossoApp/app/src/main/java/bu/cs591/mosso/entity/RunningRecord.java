package bu.cs591.mosso.entity;

import java.util.ArrayList;
import java.util.List;

public class RunningRecord {

    public String date;
    public String dayInWeek;
    public String distance;
    public String speed;
    public String duration;

    public RunningRecord(String date, String dayInWeek, String distance, String speed, String duration) {
        this.date = date;
        this.dayInWeek = dayInWeek;
        this.distance = distance;
        this.speed = speed;
        this.duration = duration;
    }

    public String getDate() {
        return date;
    }

    public String getDayInWeek() {
        return dayInWeek;
    }

    public String getDistance() {
        return distance;
    }

    public String getSpeed() {
        return speed;
    }

    public String getDuration() {
        return duration;
    }
}
