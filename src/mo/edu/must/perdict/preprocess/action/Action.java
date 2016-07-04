
package mo.edu.must.perdict.preprocess.action;

import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class Action {
    public static final int EVENT_APP_OPEN = 0;

    public static final int EVENT_LOCATION_CHANGED = 1;

    public static final int EVENT_CHARGE_CABLE = 2;

    public static final int EVENT_AUDIO_CABLE = 3;

    public static final int EVENT_CONTEXT_TRIGGER = 4;

    public static final int EVENT_CONTEXT_PULLED = 5;

    public static final int EVENT_LIGHT_CHANGED = 6;

    public static final int EVENT_WIFI_CONNECTED = 7;

    public static final int EVENT_DATA_CONNECTED = 8;

    public static final int EVENT_BLUETOOTH_CONNECTED = 9;

    protected static SimpleDateFormat format = new SimpleDateFormat("HH:mm");

    protected int eventId;

    protected Date time;

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}
