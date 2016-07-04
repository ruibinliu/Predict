package mo.edu.must.perdict.preprocess;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import mo.edu.must.perdict.preprocess.action.Action;
import mo.edu.must.perdict.preprocess.action.AppOpenAction;
import mo.edu.must.perdict.preprocess.action.AudioCableAction;
import mo.edu.must.perdict.preprocess.action.BluetoothConnectedAction;
import mo.edu.must.perdict.preprocess.action.ChargeCableAction;
import mo.edu.must.perdict.preprocess.action.Context;
import mo.edu.must.perdict.preprocess.action.ContextTriggerAction;
import mo.edu.must.perdict.preprocess.action.DataConnectedAction;
import mo.edu.must.perdict.preprocess.action.LightChangedAction;
import mo.edu.must.perdict.preprocess.action.LocationChangedAction;
import mo.edu.must.perdict.preprocess.action.WiFiConnectedAction;

public class DataProcessor {
    public static ArrayList<Action> recordToAction(RecordList recordList) {
        ArrayList<Action> actionList = new ArrayList<>();

        for (int i = 0, size = recordList.size(); i < size; i++) {
            Record record = recordList.get(i);

            String packName = record.get(Record.PACKNAME);

            Date time = null;

            try {
                time = getDateByString(record.get(Record.USETIME));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (!"insertTarget".equals(packName)) {
                // App Open
                actionList.add(new AppOpenAction(time, packName));
            } else {
                // Event
                String event = record.get(Record.EVENT);
                String data1 = record.get(Record.DATA1);

                if ("LightChanged".equals(event)) {
                    actionList.add(new LightChangedAction(time, data1));
                } else if ("WiFiConnected".equals(event)) {
                    actionList.add(new WiFiConnectedAction(time, data1));
                } else if ("DataConnected".equals(event)) {
                    actionList.add(new DataConnectedAction(time));
                } else if ("BluetoothConnected".equals(event)) {
                    actionList.add(new BluetoothConnectedAction(time, data1));
                } else if ("AudioCable".equals(event)) {
                    actionList.add(new AudioCableAction(time));
                } else if ("ChargeCable".equals(event)) {
                    actionList.add(new ChargeCableAction(time));
                } else if ("LocationChanged".equals(event)) {
                    actionList.add(new LocationChangedAction(time, data1));
                }
            }
        }

        return actionList;
    }

    public static HashMap<Action, Context> findContext(ArrayList<Action> actionList) {
        // 计算Context
        HashMap<Action, Context> actionContext = new HashMap<>();

        for (int i = 0, size = actionList.size(); i < size; i++) {
            Action action = actionList.get(i);

            if (action.getEventId() != Action.EVENT_APP_OPEN)
                continue;

            Context context = new Context();
            actionContext.put(action, context);

            for (int j = i - 1; j >= 0; j--) {
                Action procedingAction = actionList.get(j);

                // 如果两个事件事件的时间超过了一定的时间，则停止处理
                Date procedingTime = procedingAction.getTime();
                Date time = action.getTime();
                if (time.getTime() - procedingTime.getTime() > Context.GAUSSIAN_TIME) {
                    break;
                }

                switch (procedingAction.getEventId()) {
                case Action.EVENT_APP_OPEN:
                    if (context.getLastAppOpenAction() == null) {
                        context.setLastAppOpenAction((AppOpenAction) procedingAction);
                    }
                    break;
                case Action.EVENT_AUDIO_CABLE:
                    if (context.getLastAudioCableAction() == null) {
                        context.setLastAudioCableAction((AudioCableAction) procedingAction);
                    }
                    break;
                case Action.EVENT_CONTEXT_TRIGGER:
                    if (context.getLastContextTriggerAction() == null) {
                        context.setLastContextTriggerAction((ContextTriggerAction) procedingAction);
                    }
                    break;
                case Action.EVENT_LOCATION_CHANGED:
                    if (context.getLastLocationChangedAction() == null) {
                        context.setLastLocationChangedAction((LocationChangedAction) procedingAction);
                    }
                    break;
                case Action.EVENT_CHARGE_CABLE:
                    if (context.getLastChargeCableAction() == null) {
                        context.setLastChargeCableAction((ChargeCableAction) procedingAction);
                    }
                    break;
                case Action.EVENT_WIFI_CONNECTED:
                    if (context.getLastWiFiConnectedAction() == null) {
                        context.setLastWiFiConnectedAction((WiFiConnectedAction) procedingAction);
                    }
                    break;
                case Action.EVENT_DATA_CONNECTED:
                    if (context.getLastDataConnectedAction() == null) {
                        context.setLastDataConnectedAction((DataConnectedAction) procedingAction);
                    }
                    break;
                case Action.EVENT_BLUETOOTH_CONNECTED:
                    if (context.getLastBluetoothConnectedAction() == null) {
                        context.setLastBluetoothConnectedAction((BluetoothConnectedAction) procedingAction);
                    }
                    break;
                case Action.EVENT_LIGHT_CHANGED:
                    if (context.getLastLightChangedAction() == null) {
                        context.setLastLightChangedAction((LightChangedAction) procedingAction);
                    }
                    break;
                }

                if (context.getLastAppOpenAction() != null && context.getLastAudioCableAction() != null
                        && context.getLastContextTriggerAction() != null
                        && context.getLastLocationChangedAction() != null
                        && context.getLastLightChangedAction() != null
                        && context.getLastBluetoothConnectedAction() != null
                        && context.getLastWiFiConnectedAction() != null
                        && context.getLastDataConnectedAction() != null
                        && context.getLastChargeCableAction() != null) {
                    break;
                }
            }
        }

        return actionContext;
    }

    private static Date getDateByString(String time) throws ParseException {
        if (time.startsWith("2015")) {
            time = "2016" + time.substring(4);
        }

        if (time.length() < 14) {
            for (int j = (14 - time.length()); j > 0; j--) {
                time += "0";
            }
        }

        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = format.parse(time);
        return date;
    }
}
