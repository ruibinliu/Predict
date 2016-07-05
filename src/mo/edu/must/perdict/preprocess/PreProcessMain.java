
package mo.edu.must.perdict.preprocess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import mo.edu.must.perdict.preprocess.action.Action;
import mo.edu.must.perdict.preprocess.action.Context;
import mo.edu.must.perdict.utils.FileUtils;
import mo.edu.must.perdict.utils.FileUtils.Listener;

public class PreProcessMain {
    public static void main(String[] args) {
        FileUtils.read(args[0], l);

        Word2VecProcessor.process();
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

            builder = new StringBuilder();
            for (Action action : actionList) {
                builder.append(action.toString() + " ");
            }
            String words = builder.toString();

            FileUtils.write("out/words.txt", words);

            HashMap<Action, Context> actionContext = DataProcessor.findContext(actionList);

            // 准备tan的数据，将last写到文件
            builder = new StringBuilder();
            builder.append("lastAppOpenAction "
                    + "lastAudioCable "
                    + "lastLocationChanged "
                    + "lastChargeCable "
                    + "lastWiFiConnected "
                    + "lastDataConnected "
                    + "lastBluetoothConnected "
                    + "lastLightChanged "
                    + "packName\n");
            for (Action action : actionList) {
                Context context = actionContext.get(action);
                if (action.getEventId() == Action.EVENT_APP_OPEN) {
                    builder.append(context.toData() + " ");
                    builder.append(action + "\n");
                }
            }
            FileUtils.write("out/tan-data.txt", builder.toString());
        };
    };
}
