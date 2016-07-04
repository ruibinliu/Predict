package mo.edu.must.perdict.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class FileUtils {
    public static void read(String filePath, Listener l) {
        FileInputStream is = null;
        InputStreamReader isr = null;
        BufferedReader br = null;

        try {
            is = new FileInputStream(filePath);
            isr = new InputStreamReader(is);
            br = new BufferedReader(isr);

            String line;
            while ((line = br.readLine()) != null) {
                l.onReadLine(line);
            }
            l.onDone();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IoUtils.close(br);
            IoUtils.close(isr);
            IoUtils.close(is);
        }
    }

    public static void write(String filePath, String content) {
        FileOutputStream fos = null;

        File directory = new File(filePath).getParentFile();
        if (!directory.exists()) {
            directory.mkdirs();
        }
        try {
            fos = new FileOutputStream(filePath, false);

            fos.write(content.getBytes());
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IoUtils.close(fos);
        }
    }

    public abstract static class Listener {
        public void onReadLine(String line) {
        }

        public void onDone() {
        }
    }
}
