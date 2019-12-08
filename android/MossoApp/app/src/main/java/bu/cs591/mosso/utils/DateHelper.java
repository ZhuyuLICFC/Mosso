package bu.cs591.mosso.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

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

    public static String getDuration(int duration) {
        StringBuilder result = new StringBuilder();

        if (duration > 3600) {
            result.append(duration / 3600);
            result.append(":");
            duration = duration % 3600;
        }

        result.append(duration / 60);
        result.append("/");
        result.append(duration % 60);

        return result.toString();
    }


    private static String formatMinSec(int val) {
        if (val < 10) return "0" + val;
        return val + "";
    }

}
