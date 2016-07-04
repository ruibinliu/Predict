
package mo.edu.must.perdict.preprocess;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import mo.edu.must.perdict.utils.FileUtils;
import mo.edu.must.perdict.utils.IoUtils;
import mo.edu.must.perdict.utils.FileUtils.Listener;

public class Word2VecProcessor {
    public static HashMap<String, String[]> vectorMap = new HashMap<>();

    public static void process() {
        Process proc;

        InputStream is = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            proc = Runtime.getRuntime().exec("python src/word2vec.py");
            is = proc.getInputStream();
            isr = new InputStreamReader(is);
            br = new BufferedReader(isr);

            StringBuilder builder = new StringBuilder();
            String line;
            boolean isFirstLine = true;
            while ((line = br.readLine()) != null) {
                // 换行，每个词向量一行
                if (!isFirstLine && line.startsWith("AppOpenAction")
                        || line.startsWith("WiFiConnectedAction")
                        || line.startsWith("DataConnectedAction")
                        || line.startsWith("BluetoothConnectedAction")
                        || line.startsWith("LightChangedAction")
                        || line.startsWith("ContextTriggeredAction")
                        || line.startsWith("AudioCableAction")
                        || line.startsWith("ChargeCableAction")
                        || line.startsWith("LocationChangedAction")) {
                    builder.append("\n");
                }
                builder.append(line);
                isFirstLine = false;
            }
            FileUtils.write("out/vector.txt", builder.toString());
            proc.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            IoUtils.close(br);
            IoUtils.close(isr);
            IoUtils.close(is);
        }
    }

    public static void load() {
        FileUtils.read("out/vector.txt", new Listener() {
            @Override
            public void onReadLine(String line) {
                String action;
                String[] vector;

                int index = line.indexOf(" ") + 1;
                if (index > 1) {
                    action = line.substring(0, index).trim();
                    vector = line.substring(index).trim().split(" ");

                    for (String item : vector) {
                        item = item.replace("[", "");
                        item = item.replace("]", "");
                        item = item.trim();
                        if ("".equals(item)) {
                            continue;
                        }

                        Double number = doubleValue(item);
                        System.out.print(String.format("%.8f", number) + " ");
                    }
                    System.out.println(action);

//                    System.out.println(action + " " + Word2VecProcessor.toString(vector));
                } else {
                    action = line.trim();
                    vector = null;
                    System.out.println(line);
                }

                vectorMap.put(action, vector);
            }
        });
    }

    public static String toString(String[] a) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < a.length; i++) {
            builder.append(a[i]);
            if (i + 1 < a.length) {
                builder.append(" ");
            }
        }
        return builder.toString();
    }

    private static double doubleValue(String item) {
        double number;
        item = item.toLowerCase();
        if (item.indexOf("e-") > 0) {
            number = Double.valueOf(item.trim());
        } else {
            number = Double.valueOf(item.trim());
        }
        return number;
    }
}
