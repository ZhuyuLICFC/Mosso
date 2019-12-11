package bu.cs591.mosso.utils;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DateHelper {

    public static String generateTimeStamp() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        return simpleDateFormat.format(new Date());
    }

    public static String generateDayInWeek() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE");
        return simpleDateFormat.format(new Date());
    }

    public static String getDateInfo(String timestamp) {
        return timestamp.substring(4, 6) + "/" +  timestamp.substring(6, 8) + "/" +  timestamp.substring(0, 4);
    }

    public static int getDurationDiff(String startTime) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String endTime = generateTimeStamp();
        int diff = 0;
        try {
            Date start = simpleDateFormat.parse(startTime);
            Date end = simpleDateFormat.parse(endTime);
            diff = (int)TimeUnit.SECONDS.convert(Math.abs(start.getTime() - end.getTime()), TimeUnit.MILLISECONDS);
            Log.d("testo", "" + diff);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return diff;
    }

    public static String getDuration(int duration) {
        StringBuilder result = new StringBuilder();

        if (duration > 3600) {
            result.append(duration / 3600);
            result.append(":");
            duration = duration % 3600;
        }

        result.append(formatMinSec(duration / 60));
        result.append(":");
        result.append(formatMinSec(duration % 60));

        Log.d("testo", "duration:" + duration + "");
        return result.toString();
    }


    private static String formatMinSec(int val) {
        if (val < 10) return "0" + val;
        return val + "";
    }

}
