
package mo.edu.must.perdict.tan;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import mo.edu.must.perdict.utils.FileUtils;
import mo.edu.must.perdict.utils.FileUtils.Listener;

public class TanMain {
    static HashMap<String, Double> probabilityMap = new HashMap<String, Double>();

    public static void main(String[] args) {
        String filePath = args[0];
        long t0, t1;
        t0 = System.currentTimeMillis();
        TANTool tool = new TANTool(filePath);
        t1 = System.currentTimeMillis();
        long trainingCost = t1 - t0;

        final ArrayList<String> packNameList = new ArrayList<>();
        final ArrayList<String> audioCableList = new ArrayList<>();
        final ArrayList<String> locationChangedList = new ArrayList<>();
        final ArrayList<String> chargeCablesList = new ArrayList<>();
        final ArrayList<String> wifiConnectedList = new ArrayList<>();
        final ArrayList<String> dataConnectedList = new ArrayList<>();
        final ArrayList<String> bluetoothConnectedList = new ArrayList<>();
        final ArrayList<String> lightChangedList = new ArrayList<>();
        FileUtils.read(filePath, new Listener() {
            @Override
            public void onReadLine(String line) {
                if (line.startsWith("lastAppOpenAction")) {
                    return;
                }

                String[] array = line.split(" ");

                if (!packNameList.contains(array[0])) {
                    packNameList.add(array[0]);
                }
                if (!audioCableList.contains(array[1])) {
                    audioCableList.add(array[1]);
                }
                if (!locationChangedList.contains(array[2])) {
                    locationChangedList.add(array[2]);
                }
                if (!chargeCablesList.contains(array[3])) {
                    chargeCablesList.add(array[3]);
                }
                if (!wifiConnectedList.contains(array[4])) {
                    wifiConnectedList.add(array[4]);
                }
                if (!dataConnectedList.contains(array[5])) {
                    dataConnectedList.add(array[5]);
                }
                if (!bluetoothConnectedList.contains(array[6])) {
                    bluetoothConnectedList.add(array[6]);
                }
                if (!lightChangedList.contains(array[7])) {
                    lightChangedList.add(array[7]);
                }
            }
        });

        long preditCost = 0;
        int preditTimes = 0;
        long validCost = 0;
        int validTimes = 0;
        for (int i = 1; i < packNameList.size(); i++) {
            for (int j = 0; j < audioCableList.size(); j++) {
                for (int k = 1; k < locationChangedList.size(); k++) {
                    for (int l = 1; l < chargeCablesList.size(); l++) {
                        for (int m = 1; m < wifiConnectedList.size(); m++) {
                            for (int n = 1; n < dataConnectedList.size(); n++) {
                                for (int o = 0; o < bluetoothConnectedList.size(); o++) {
                                    for (int p = 1; p < lightChangedList.size(); p++) {

                                        // Precondition
                                        Precondition precondition = new Precondition();
                                        precondition.setLastAppOpenAction(packNameList.get(i));
                                        precondition.setLastAudioCableAction(audioCableList.get(j));
                                        precondition
                                                .setLastLocationChangedAction(locationChangedList
                                                        .get(k));
                                        precondition.setLastChargeCableAction(chargeCablesList
                                                .get(l));
                                        precondition.setLastWiFiConnectedAction(wifiConnectedList
                                                .get(m));
                                        precondition.setLastDataConnectedAction(dataConnectedList
                                                .get(n));
                                        precondition
                                                .setLastBluetoothConnectedAction(bluetoothConnectedList
                                                        .get(o));
                                        precondition.setLastLightChangedAction(lightChangedList
                                                .get(p));
                                        System.out.println(precondition.toString());

                                        for (int q = 0; q < packNameList.size(); q++) {
                                            t0 = System.currentTimeMillis();
                                            String packName = packNameList.get(q);
                                            double propability = getProbability(tool, precondition,
                                                    packName);
                                            probabilityMap.put(packName, propability);
                                            t1 = System.currentTimeMillis();
                                            preditCost += (t1 - t0);
                                            preditTimes++;
                                        }

                                        // Soft the packNameList by
                                        // probability.
                                        ArrayList<String> sortedPackList = new ArrayList<>();
                                        sortedPackList.addAll(probabilityMap.keySet());
                                        Collections.sort(sortedPackList, new Comparator<String>() {

                                            @Override
                                            public int compare(String o1, String o2) {
                                                double p1 = probabilityMap.get(o1);
                                                double p2 = probabilityMap.get(o2);

                                                return (int)(((1 - p1) * 1000000000) - ((1 - p2) * 1000000000));
                                            }
                                        });

                                        for (String packName : sortedPackList) {
                                            System.out.println("=========");
                                            System.out.println(precondition.toString());
                                            System.out.println("分类类别为packName=" + packName + ", "
                                                    + probabilityMap.get(packName));
                                        }

                                        for (int r = 0; r < sortedPackList.size(); r++) {
                                            String preditedPackName = sortedPackList.get(r);
                                            if (r == 0) {
                                                t0 = System.currentTimeMillis();
                                                double accuracy = validate(precondition,
                                                        preditedPackName, true);
                                                t1 = System.currentTimeMillis();
                                                validCost += (t1 - t0);
                                                validTimes++;
                                                if (accuracy != 0) {
                                                    sValidationTimes++;
                                                    sAccuracySum += accuracy;
                                                }
                                            } else {
                                                t0 = System.currentTimeMillis();
                                                double accuracy = validate(precondition,
                                                        preditedPackName, false);
                                                t1 = System.currentTimeMillis();
                                                validCost += (t1 - t0);
                                                validTimes++;
                                            }
                                        }

                                        System.out.println("Training Cost: " + trainingCost + " ms");
                                        System.out.println("Predit Cost: " + preditCost + " ms, Predit " + preditTimes
                                                + " Times, Average Cost: " + ((double)preditCost / (double)preditTimes) + " ms");
                                        System.out.println("Validation Cost: " + validCost + " ms");
                                        System.out.println("Average Accuracy: " + (sAccuracySum / sValidationTimes) + "("
                                                + sAccuracySum + "/" + sValidationTimes + ")");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static String sValidation = "";

    private static int sValidationTimes = 0;

    private static double sAccuracySum = 0;

    private static double validate(Precondition precondition, String preditedPackName, boolean print) {
        if ("".equals(sValidation.trim())) {
            String filePath = "/Users/ruibin/git/github/Predit/tan-test.txt";

            FileInputStream fis = null;
            InputStreamReader isr = null;
            BufferedReader br = null;

            try {
                fis = new FileInputStream(filePath);
                isr = new InputStreamReader(fis);
                br = new BufferedReader(isr);

                String line;
                while ((line = br.readLine()) != null) {
                    sValidation += line + "\n";
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                close(isr);
                close(fis);
            }
        }

        String[] lines = sValidation.split("\n");
        double matched = 0;
        double unmatched = 0;

        String preconditionPrefix = String.format("%s %s %s %s %s %s %s %s",
                precondition.getLastAppOpenAction(), precondition.getLastAudioCableAction(),
                precondition.getLastLocationChangedAction(),
                precondition.getLastChargeCableAction(), precondition.getLastWiFiConnectedAction(),
                precondition.getLastDataConnectedAction(),
                precondition.getLastBluetoothConnectedAction(),
                precondition.getLastLightChangedAction());

        for (String line : lines) {
            if (line.startsWith(preconditionPrefix)) {
                if (line.endsWith(preditedPackName)) {
                    matched++;
                } else {
                    unmatched++;
                }
            }
        }

        double total = matched + unmatched;
        double accuracy;
        if (total > 0) {
            accuracy = matched / total;
        } else {
            accuracy = 0;
        }

        if (print) {
            System.out.println(precondition.toString() + ", preditedPackName: " + preditedPackName
                    + ", matched: " + matched + ", unmatched: " + unmatched + ", total: " + total
                    + ", accuracy: " + accuracy);
        }
        return accuracy;
    }

    private static void close(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
            }
        }
    }

    private static double getProbability(TANTool tool, Precondition precondition, String packName) {
        // Precondition
        String lastAppOpened = precondition.getLastAppOpenAction();
        String lastAudioCable = precondition.getLastAudioCableAction();
        String lastLocationUpdated = precondition.getLastLocationChangedAction();
        String lastChargeCable = precondition.getLastChargeCableAction();
        String lastWiFiConnected = precondition.getLastWiFiConnectedAction();
        String lastDataConnected = precondition.getLastDataConnectedAction();
        String lastBluetoothConnected = precondition.getLastBluetoothConnectedAction();
        String lastLightChanged = precondition.getLastLightChangedAction();

        String queryStr = String.format(
                "lastAppOpenedAction=%s," + "lastAudioCableAction=%s,"
                        + "lastLocationChangedAction=%s," + "lastChargeCableAction=%s,"
                        + "lastWiFiConnectedAction=%s," + "lastDataConnectedAction=%s,"
                        + "lastBluetoothConnectedAction=%s," + "lastLightChangedAction=%s,"
                        + "packName=%s", lastAppOpened, lastAudioCable, lastLocationUpdated,
                lastChargeCable, lastWiFiConnected, lastDataConnected, lastBluetoothConnected,
                lastLightChanged, packName);
        double prop = tool.calHappenedPro(queryStr);
        System.out.println("queryStr=" + queryStr + ", " + prop);
        return prop;
    }
}
