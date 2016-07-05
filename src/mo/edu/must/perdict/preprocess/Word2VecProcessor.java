
package mo.edu.must.perdict.preprocess;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import mo.edu.must.perdict.utils.FileUtils;
import mo.edu.must.perdict.utils.IoUtils;

public class Word2VecProcessor {

    private static final String PYTHON_COMMAND = "python src/word2vec.py";

    public static void process() {
        Process proc;

        InputStream is = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            proc = Runtime.getRuntime().exec(PYTHON_COMMAND);
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
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            IoUtils.close(br);
            IoUtils.close(isr);
            IoUtils.close(is);
        }
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
}
