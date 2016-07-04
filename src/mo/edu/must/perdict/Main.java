
package mo.edu.must.perdict;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import mo.edu.must.perdict.preprocess.DataProcessor;
import mo.edu.must.perdict.preprocess.Record;
import mo.edu.must.perdict.preprocess.RecordList;
import mo.edu.must.perdict.preprocess.Word2VecProcessor;
import mo.edu.must.perdict.preprocess.action.Action;
import mo.edu.must.perdict.preprocess.action.Context;
import mo.edu.must.perdict.tan.TanMain;
import mo.edu.must.perdict.utils.FileUtils;
import mo.edu.must.perdict.utils.IoUtils;
import mo.edu.must.perdict.utils.FileUtils.Listener;

public class Main {
    public static void main(String[] args) {
        String filePath = "data/raw.txt";
        FileUtils.read(filePath, l);

        TanMain.main(new String[] {
            "out/tan-data.txt"
        });
    }

    static Listener l = new Listener() {
        RecordList recordList = new RecordList();

        @Override
        public void onReadLine(String line) {
            Record record = new Record();
            record.parse(line);
            recordList.add(record);
        }

        @Override
        public void onDone() {
            ArrayList<Action> actionList = DataProcessor.recordToAction(recordList);
            HashSet<String> actionSet = new HashSet<>();
            for (Action action : actionList) {
                String actionString = action.toString();
                if (!actionSet.contains(actionString)) {
                    actionSet.add(actionString);
                }
            }
            StringBuilder builder = new StringBuilder();
            for (String action : actionSet) {
                builder.append(action + "\n");
            }
            FileUtils.write("out/set.txt", builder.toString());

            // for (String action : actionSet) {
            // System.out.println(action);
            // }

            builder = new StringBuilder();
            for (Action action : actionList) {
                builder.append(action.toString() + " ");
            }
            String words = builder.toString();
            // System.out.println(words);

            FileUtils.write("out/words.txt", words);

            HashMap<Action, Context> actionContext = DataProcessor.findContext(actionList);

            // 准备tan的数据
            builder = new StringBuilder();
            builder.append("lastAppOpenAction lastAudioCableAction "
                    + "lastLocationChangedAction lastChargeCableAction "
                    + "lastWiFiConnectedAction lastDataConnectedAction "
                    + "lastBluetoothConnectedAction lastLightChangedAction " + "packName\n");
            int appOpenActionCount = 0;
            for (Action action : actionList) {
                if (action.getEventId() == Action.EVENT_APP_OPEN) {
                    appOpenActionCount++;
                }
            }

            // 将last写到文件
            int traningCount = appOpenActionCount / 2;
            StringBuilder testBuilder = new StringBuilder();
            for (Action action : actionList) {
                Context context = actionContext.get(action);
                if (action.getEventId() == Action.EVENT_APP_OPEN) {
                    if (traningCount > 0) {
                        traningCount--;
                        builder.append(context.toData() + " ");
                        builder.append(action + "\n");
                    } else {
                        testBuilder.append(context.toData() + " ");
                        testBuilder.append(action + "\n");
                    }
                }
            }
            FileUtils.write("out/tan-data.txt", builder.toString());
            FileUtils.write("out/tan-test.txt", testBuilder.toString());

            Word2VecProcessor.process();
            Word2VecProcessor.load();
        };
    };

    public static HashMap<String, String[]> getVectorMap() {
        File vectorMapFile = new File(
                "out/vector_map.txt");

        HashMap<String, String[]> vectorMap = new HashMap<>();

        FileReader fr = null;
        BufferedReader br = null;
        try {
            fr = new FileReader(vectorMapFile);
            br = new BufferedReader(fr);

            String line;
            while ((line = br.readLine()) != null) {
                String[] split = line.split(" ");
                String packName = split[0];
                String[] vector = split[1].split(",");
                vectorMap.put(packName, vector);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IoUtils.close(br);
            IoUtils.close(fr);
        }

        return vectorMap;
    }

}
