package bu.cs591.mosso.entity;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

public class RunningRecord {

    public static List<RunningRecord> runningRecords = new ArrayList<>();

    private String date;
    private String dayInWeek;
    private String distance;
    private String speed;
    private String duration;
    private Bitmap runningRoute;

    public RunningRecord(String date, String dayInWeek, String distance, String speed, String duration, Bitmap runningRoute) {
        this.date = date;
        this.dayInWeek = dayInWeek;
        this.distance = distance;
        this.speed = speed;
        this.duration = duration;
        this.runningRoute = runningRoute;
    }

    public static List<RunningRecord> getRunningRecords() {
        return runningRecords;
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

    public Bitmap getRunningRoute() {
        return runningRoute;
    }
}
