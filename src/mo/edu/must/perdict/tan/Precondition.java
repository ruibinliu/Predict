
package mo.edu.must.perdict.tan;

public class Precondition {

    private String lastAppOpenAction;

    private String lastAudioCableAction;

    private String lastContextTriggerAction;

    private String lastLocationChangedAction;

    private String lastChargeCableAction;

    private String lastWiFiConnectedAction;

    private String lastDataConnectedAction;

    private String lastBluetoothConnectedAction;

    private String lastLightChangedAction;

    public String getLastAppOpenAction() {
        return lastAppOpenAction;
    }

    public void setLastAppOpenAction(String lastAppOpenAction) {
        this.lastAppOpenAction = lastAppOpenAction;
    }

    public String getLastAudioCableAction() {
        return lastAudioCableAction;
    }

    public void setLastAudioCableAction(String lastAudioCableAction) {
        this.lastAudioCableAction = lastAudioCableAction;
    }

    public String getLastContextTriggerAction() {
        return lastContextTriggerAction;
    }

    public void setLastContextTriggerAction(String lastContextTriggerAction) {
        this.lastContextTriggerAction = lastContextTriggerAction;
    }

    public String getLastLocationChangedAction() {
        return lastLocationChangedAction;
    }

    public void setLastLocationChangedAction(String lastLocationChangedAction) {
        this.lastLocationChangedAction = lastLocationChangedAction;
    }

    public String getLastChargeCableAction() {
        return lastChargeCableAction;
    }

    public void setLastChargeCableAction(String lastChargeCableAction) {
        this.lastChargeCableAction = lastChargeCableAction;
    }

    public String getLastWiFiConnectedAction() {
        return lastWiFiConnectedAction;
    }

    public void setLastWiFiConnectedAction(String lastWiFiConnectedAction) {
        this.lastWiFiConnectedAction = lastWiFiConnectedAction;
    }

    public String getLastDataConnectedAction() {
        return lastDataConnectedAction;
    }

    public void setLastDataConnectedAction(String lastDataConnectedAction) {
        this.lastDataConnectedAction = lastDataConnectedAction;
    }

    public String getLastBluetoothConnectedAction() {
        return lastBluetoothConnectedAction;
    }

    public void setLastBluetoothConnectedAction(String lastBluetoothConnectedAction) {
        this.lastBluetoothConnectedAction = lastBluetoothConnectedAction;
    }

    public String getLastLightChangedAction() {
        return lastLightChangedAction;
    }

    public void setLastLightChangedAction(String lastLightChangedAction) {
        this.lastLightChangedAction = lastLightChangedAction;
    }

    @Override
    public String toString() {
        return "Precondition [lastAppOpenAction=" + lastAppOpenAction + ", lastAudioCableAction="
                + lastAudioCableAction + ", lastContextTriggerAction=" + lastContextTriggerAction
                + ", lastLocationChangedAction=" + lastLocationChangedAction
                + ", lastChargeCableAction=" + lastChargeCableAction + ", lastWiFiConnectedAction="
                + lastWiFiConnectedAction + ", lastDataConnectedAction=" + lastDataConnectedAction
                + ", lastBluetoothConnectedAction=" + lastBluetoothConnectedAction
                + ", lastLightChangedAction=" + lastLightChangedAction + "]";
    }
}
