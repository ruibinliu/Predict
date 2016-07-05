
package mo.edu.must.perdict.tan;

import java.util.ArrayList;
import java.util.HashMap;

import mo.edu.must.perdict.utils.CollectionUtils;
import mo.edu.must.perdict.utils.FileUtils;
import mo.edu.must.perdict.utils.FileUtils.Listener;

public class TanMain {
    static HashMap<String, Double> probabilityMap = new HashMap<String, Double>();
    public static String verifyFilePath;
    private static final int CROSS_VALIDATION_FOLDS = 10;

    public static void main(String[] args) {
        final String dataFilePath = args[0];
        for (int fold = 0; fold < CROSS_VALIDATION_FOLDS; fold++) {
            String traningFilePath = "out/tan-data-" + fold + ".txt";
            verifyFilePath = "out/tan-test-" + fold + ".txt";

            // Cross-Validation
            final ArrayList<String> lines = new ArrayList<>();
            FileUtils.read(dataFilePath, new Listener() {
                @Override
                public void onReadLine(String line) {
                    lines.add(line);
                }
            });
            int size = lines.size();
            final StringBuilder trainingBuilder = new StringBuilder(lines.get(0) + "\n");
            final StringBuilder testBuilder = new StringBuilder();
            int testIndexStart = (size * fold / CROSS_VALIDATION_FOLDS) + 1; // 第一行是说明数据类型
            int testIndexEnd = (size * (fold + 1) / CROSS_VALIDATION_FOLDS) + 1;
            for (int i = 1; i < size; i++) {
                String line = lines.get(i);
                if (i >= testIndexStart && i < testIndexEnd) {
                    testBuilder.append(line + "\n");
                } else {
                    trainingBuilder.append(line + "\n");
                }
            }
            FileUtils.write(traningFilePath, trainingBuilder.toString());
            FileUtils.write(verifyFilePath, testBuilder.toString());

            long t0, t1;
            t0 = System.currentTimeMillis();
            TANTool tool = new TANTool(traningFilePath);
            t1 = System.currentTimeMillis();
            long trainingCost = t1 - t0;

            final ArrayList<String> packNameList = new ArrayList<>();
            final HashMap<String, Precondition> m = new HashMap<>();

            FileUtils.read(traningFilePath, new Listener() {
                @Override
                public void onReadLine(String line) {
                    if (line.startsWith("lastAppOpenAction")) {
                        return;
                    }

                    String[] array = line.split(" ");
                    // Precondition
                    Precondition precondition = new Precondition();
                    if (!packNameList.contains(array[0])) {
                        packNameList.add(array[0]);
                    }
                    precondition.setLastAppOpenAction(array[0]);
                    precondition.setLastAudioCableAction(array[1]);
                    precondition.setLastLocationChangedAction(array[2]);
                    precondition.setLastChargeCableAction(array[3]);
                    precondition.setLastWiFiConnectedAction(array[4]);
                    precondition.setLastDataConnectedAction(array[5]);
                    precondition.setLastBluetoothConnectedAction(array[6]);
                    precondition.setLastLightChangedAction(array[7]);
                    
                    if (!m.containsKey(precondition.toString())) {
                        m.put(precondition.toString(), precondition);
                    }
                }
            });
            
            long preditCost = 0;
            int preditTimes = 0;
            long validCost = 0;
            int validTimes = 0;
            for (Precondition precondition : m.values()) {
                for (int q = 0; q < packNameList.size(); q++) {
                    t0 = System.currentTimeMillis();
                    String packName = packNameList.get(q);
                    if ("null".equals(packName)) {
                        continue;
                    }
                    double propability = getProbability(tool, precondition, packName);
                    probabilityMap.put(packName, propability);
                    t1 = System.currentTimeMillis();
                    preditCost += (t1 - t0);
                    preditTimes++;
                }

                ArrayList<String> packList = new ArrayList<>();
                packList.addAll(probabilityMap.keySet());
                
                // Sort the packNameList by probability.
                double[] probabilities = new double[probabilityMap.size()];
                for (int i = 0; i < probabilityMap.size(); i++) {
                    probabilities[i] = probabilityMap.get(packList.get(i));
                }
                CollectionUtils.bubbleSort(probabilities);
                ArrayList<String> sortedPackList = new ArrayList<>();
                for (double p : probabilities) {
                    for (String packName : packList) {
                        if (probabilityMap.get(packName) == p) {
                            sortedPackList.add(packName);
                        }
                    }
                }
                
                for (int r = 0; r < packList.size(); r++) {
                    String preditedPackName = packList.get(r);
                    t0 = System.currentTimeMillis();
                    double accuracy = validate(precondition, preditedPackName, true);
                    t1 = System.currentTimeMillis();
                    validCost += (t1 - t0);
                    validTimes++;
                    if (accuracy != 0) {
                        sValidationTimes++;
                        sAccuracySum += accuracy;
                    }
                }

                System.out.println("Training Cost: " + trainingCost + " ms");
                System.out
                .println("Predit Cost: " + preditCost + " ms, Predit " + preditTimes
                        + " Times, Average Cost: " + ((double)preditCost / (double)preditTimes)
                        + " ms");
                System.out.println("Validation Cost: " + validCost + " ms");
                System.out.println("Average Accuracy: " + (sAccuracySum / sValidationTimes) + "("
                        + sAccuracySum + "/" + sValidationTimes + ")");
            }
        }
    }

    private static String sValidation = "";

    private static int sValidationTimes = 0;

    private static double sAccuracySum = 0;

    private static double validate(Precondition precondition, String preditedPackName, boolean print) {
        if ("".equals(sValidation.trim())) {
            FileUtils.read(verifyFilePath, new Listener() {
                @Override
                public void onReadLine(String line) {
                    sValidation += line + "\n";
                }
            });
        }

        String[] lines = sValidation.split("\n");
        double matched = 0;
        double unmatched = 0;

        String preconditionPrefix = String.format("%s %s %s %s %s %s %s %s",
                precondition.getLastAppOpenAction(),
                precondition.getLastAudioCableAction(),
                precondition.getLastLocationChangedAction(),
                precondition.getLastChargeCableAction(),
                precondition.getLastWiFiConnectedAction(),
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
        double accuracy = total > 0 ? matched / total : 0;

        System.out.println(precondition.toString() + ", preditedPackName: " + preditedPackName
                + ", matched: " + matched + ", unmatched: " + unmatched + ", total: " + total
                + ", accuracy: " + accuracy);
        return accuracy;
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

        String queryStr = String.format("lastAppOpenAction=%s,"
                + "lastAudioCable=%s,"
                + "lastLocationUpdated=%s,"
                + "lastChargeCable=%s,"
                + "lastWiFiConnected=%s,"
                + "lastDataConnected=%s,"
                + "lastBluetoothConnected=%s,"
                + "lastLightChanged=%s,"
                + "packName=%s",
                lastAppOpened,
                lastAudioCable,
                lastLocationUpdated,
                lastChargeCable,
                lastWiFiConnected,
                lastDataConnected,
                lastBluetoothConnected,
                lastLightChanged,
                packName);
        double prop = tool.calHappenedPro(queryStr);
        System.out.println("queryStr=" + queryStr + ", " + prop);
        return prop;
    }

    public static class Accumulator {
        private int value = 0;

        public void add() {
            value++;
        }

        public int getValue() {
            return value;
        }
    }
}
