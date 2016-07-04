package mo.edu.must.perdict.preprocess;

import java.util.HashMap;

public class Record extends HashMap<String, String> {
    public static final String PACKNAME = "packName";
    public static final String USETIME = "useTime";
    public static final String USEPERIOD = "usePeriod";
    public static final String USESECOND = "useSecond";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longtitude";
    public static final String IS_WORK = "isWork";
    public static final String GPRS = "gprs";
    public static final String WIFINAME = "wifiName";
    public static final String BLUETOOTH = "bluetooth";
    public static final String WEEKDAY = "weekday";
    public static final String HEADPHONE = "headPhone";
    public static final String LIGHT = "light";
    public static final String ACTION = "action";
    public static final String NOTIFICATION = "notification";
    public static final String EVENT = "event";
    public static final String DATA1 = "data1";
    public static final String LOCATION = "location";

    public void parse(String line) {
        String[] words = line.split(" ");

        put(Record.PACKNAME, words[0]);
        put(Record.USETIME, words[1]);
        put(Record.USEPERIOD, words[2]);
        put(Record.USESECOND, words[3]);
        put(Record.LATITUDE, words[4]);
        put(Record.LONGITUDE, words[5]);
        put(Record.IS_WORK, words[6]);
        put(Record.GPRS, words[7]);
        put(Record.WIFINAME, words[8]);
        put(Record.BLUETOOTH, words[9]);
        put(Record.WEEKDAY, words[10]);
        put(Record.HEADPHONE, words[11]);
        put(Record.LIGHT, words[12]);
        put(Record.ACTION, words[13]);
        put(Record.NOTIFICATION, words[14]);
        put(Record.EVENT, words[16]);
        put(Record.DATA1, words[17]);
        put(Record.LOCATION, words[18]);
    }
}
