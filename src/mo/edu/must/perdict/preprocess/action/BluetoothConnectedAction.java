package mo.edu.must.perdict.preprocess.action;

import java.util.Date;

public class BluetoothConnectedAction extends Action {
    private String deviceName;

    public BluetoothConnectedAction(Date time, String deviceName) {
        eventId = EVENT_BLUETOOTH_CONNECTED;
        this.time = time;
        this.deviceName = deviceName;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    @Override
    public String toString() {
        return "BluetoothConnectedAction[" + deviceName + "]";
    }
}
