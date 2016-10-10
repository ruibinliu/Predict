package mo.edu.must.perdict.preprocess;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import mo.edu.must.perdict.utils.FileUtils;
import mo.edu.must.perdict.utils.FileUtils.Listener;

public class MdcPreprocess {
    public static void main(String[] args) {
        FileUtils.read("data/mdc.txt", new Listener() {
            List<String> lines = new ArrayList<>();

            @Override
            public void onReadLine(String line) {
                if (line.startsWith("screensaver")) return;
                if (line.startsWith("Standby")) return;

//                line = line.replaceAll(" ", "_");
//                if (line.startsWith("Clock_")) {
//                    line = line.substring(0, "Clock".length());
//                }
//                if (line.startsWith("Local_")) {
//                    line = line.substring(0, "Local".length());
//                }
//                if (line.startsWith("Standby_mode")) {
//                    line = line.substring(0, "Standby_mode".length());
//                }
//                if (line.startsWith("screensaver")) {
//                    line = line.substring(0, "screensaver".length());
//                }
                lines.add(line);
            }

            @Override
            public void onDone() {
                for (int i = 1, size = lines.size(); i < size; i++) {
                    String previousLine = lines.get(i - 1);
                    String currentLine = lines.get(i);
                    String lastApp = handleApp(previousLine.substring(0, previousLine.lastIndexOf(",")).replaceAll(" ", "-"));
                    String currentApp = handleApp(currentLine.substring(0, currentLine.lastIndexOf(",")).replaceAll(" ", "-"));

                    if (!lastApp.equals(currentApp)) {
                        String time = currentLine.substring(currentLine.lastIndexOf(",") + 1);
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        time = format.format(new Date(Long.valueOf(time) * 1000));
                        try {
                            long timeMillis = format.parse(time).getTime();
                            System.out.println(lastApp + "," + timeMillis + "," + currentApp);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            private String handleApp(String app) {
                String[] filter = new String[] {"Local", "Clock", "Standby-mode", "screensaver", "Py_", "MMFControllerProxyServer", "ICLThread", "MapsAudioThread"};

                for (String key : filter) {
                    if (app.startsWith(key)) {
                        app = key;
                        break;
                    }
                }

                return app;
            }
        });
    }
}
