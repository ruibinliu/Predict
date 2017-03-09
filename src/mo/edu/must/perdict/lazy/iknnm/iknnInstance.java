package mo.edu.must.perdict.lazy.iknnm;

/**
 * Created by HackerZ on 2017/3/8.
 */
public class iknnInstance {
    private String lastApp;

    private String lastAudio;

    private String lastLocation;

    private String lastCharge;

    private String lastWifi;

    private String lastData;

    private String lastBluetooth;

    private String lastLight;

    private String app;
    // 向量
    private double vector[];

    public iknnInstance(String line) {
        String[] split = line.split(" ");
        int length = split.length;
        vector = new double[length-1];
        int i = -1;
        for(String vc : split){
            if(i == -1) {
                app = vc;
            }else{
                vector[i] = Double.parseDouble(vc);
            }
            i++;
        }
//        lastApp = split[0];
//        lastAudio = split[1];
//        lastLocation = split[2];
//        lastCharge = split[3];
//        lastWifi = split[4];
//        lastData = split[5];
//        lastBluetooth = split[6];
//        lastLight = split[7];
//        app = split[8];
    }

    public String getLastApp() {
        return lastApp;
    }

    public void setLastApp(String lastApp) {
        this.lastApp = lastApp;
    }

    public String getLastAudio() {
        return lastAudio;
    }

    public void setLastAudio(String lastAudio) {
        this.lastAudio = lastAudio;
    }

    public String getLastLocation() {
        return lastLocation;
    }

    public void setLastLocation(String lastLocation) {
        this.lastLocation = lastLocation;
    }

    public String getLastCharge() {
        return lastCharge;
    }

    public void setLastCharge(String lastCharge) {
        this.lastCharge = lastCharge;
    }

    public String getLastWifi() {
        return lastWifi;
    }

    public void setLastWifi(String lastWifi) {
        this.lastWifi = lastWifi;
    }

    public String getLastData() {
        return lastData;
    }

    public void setLastData(String lastData) {
        this.lastData = lastData;
    }

    public String getLastBluetooth() {
        return lastBluetooth;
    }

    public void setLastBluetooth(String lastBluetooth) {
        this.lastBluetooth = lastBluetooth;
    }

    public String getLastLight() {
        return lastLight;
    }

    public void setLastLight(String lastLight) {
        this.lastLight = lastLight;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public double[] getVector() {return vector;}
}