package mo.edu.must.perdict.data.action;

import java.util.Date;

public class WiFiConnectedAction extends Action {
    private String wifiName;

    public WiFiConnectedAction(Date time, String wifiName) {
        eventId = EVENT_WIFI_CONNECTED;
        this.time = time;
        this.wifiName = wifiName;
    }

    @Override
    public String toString() {
        return "WiFiConnectedAction[wifiName=" + wifiName + "]";
    }
}
