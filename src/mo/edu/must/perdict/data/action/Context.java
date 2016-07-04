
package mo.edu.must.perdict.data.action;

public class Context {
    public static final int GAUSSIAN_TIME = 10 * 60 * 1000;

    private AppOpenAction lastAppOpenAction;

    private AudioCableAction lastAudioCableAction;

    private ContextTriggerAction lastContextTriggerAction;

    private LocationChangedAction lastLocationChangedAction;

    private ChargeCableAction lastChargeCableAction;

    private WiFiConnectedAction lastWiFiConnectedAction;

    private DataConnectedAction lastDataConnectedAction;

    private BluetoothConnectedAction lastBluetoothConnectedAction;

    private LightChangedAction lastLightChangedAction;

    public AppOpenAction getLastAppOpenAction() {
        return lastAppOpenAction;
    }

    public void setLastAppOpenAction(AppOpenAction lastAppOpenAction) {
        this.lastAppOpenAction = lastAppOpenAction;
    }

    public AudioCableAction getLastAudioCableAction() {
        return lastAudioCableAction;
    }

    public void setLastAudioCableAction(AudioCableAction lastAudioCableAction) {
        this.lastAudioCableAction = lastAudioCableAction;
    }

    public ContextTriggerAction getLastContextTriggerAction() {
        return lastContextTriggerAction;
    }

    public void setLastContextTriggerAction(ContextTriggerAction lastContextTriggerAction) {
        this.lastContextTriggerAction = lastContextTriggerAction;
    }

    public LocationChangedAction getLastLocationChangedAction() {
        return lastLocationChangedAction;
    }

    public void setLastLocationChangedAction(LocationChangedAction lastLocationChangedAction) {
        this.lastLocationChangedAction = lastLocationChangedAction;
    }

    public ChargeCableAction getLastChargeCableAction() {
        return lastChargeCableAction;
    }

    public void setLastChargeCableAction(ChargeCableAction lastChargeCableAction) {
        this.lastChargeCableAction = lastChargeCableAction;
    }

    public WiFiConnectedAction getLastWiFiConnectedAction() {
        return lastWiFiConnectedAction;
    }

    public void setLastWiFiConnectedAction(WiFiConnectedAction lastWiFiConnectedAction) {
        this.lastWiFiConnectedAction = lastWiFiConnectedAction;
    }

    public DataConnectedAction getLastDataConnectedAction() {
        return lastDataConnectedAction;
    }

    public void setLastDataConnectedAction(DataConnectedAction lastDataConnectedAction) {
        this.lastDataConnectedAction = lastDataConnectedAction;
    }

    public BluetoothConnectedAction getLastBluetoothConnectedAction() {
        return lastBluetoothConnectedAction;
    }

    public void setLastBluetoothConnectedAction(
            BluetoothConnectedAction lastBluetoothConnectedAction) {
        this.lastBluetoothConnectedAction = lastBluetoothConnectedAction;
    }

    public LightChangedAction getLastLightChangedAction() {
        return lastLightChangedAction;
    }

    public void setLastLightChangedAction(LightChangedAction lastLightChangedAction) {
        this.lastLightChangedAction = lastLightChangedAction;
    }

    @Override
    public String toString() {
        return "Context [lastAppOpenAction=" + lastAppOpenAction + ", lastAudioCableAction="
                + lastAudioCableAction + ", lastContextTriggerAction=" + lastContextTriggerAction
                + ", lastLocationChangedAction=" + lastLocationChangedAction
                + ", lastChargeCableAction=" + lastChargeCableAction + ", lastWiFiConnectedAction="
                + lastWiFiConnectedAction + ", lastDataConnectedAction=" + lastDataConnectedAction
                + ", lastBluetoothConnectedAction=" + lastBluetoothConnectedAction
                + ", lastLightChangedAction=" + lastLightChangedAction + "]";
    }

    public String toData() {
        return lastAppOpenAction + " " + lastAudioCableAction + " " + lastLocationChangedAction
                + " " + lastChargeCableAction + " " + lastWiFiConnectedAction + " "
                + lastDataConnectedAction + " " + lastBluetoothConnectedAction + " "
                + lastLightChangedAction;
    }
}
