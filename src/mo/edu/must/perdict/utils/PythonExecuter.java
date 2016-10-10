package mo.edu.must.perdict.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class PythonExecuter {
    public static String process(String command) {
        System.out.println("Executing command: " + command);
        Process proc;

        InputStream is = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            proc = Runtime.getRuntime().exec(command);
            is = proc.getInputStream();
            isr = new InputStreamReader(is);
            br = new BufferedReader(isr);

            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                builder.append(line);
            }
            String result = builder.toString().trim();
            proc.waitFor();
            return result;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        } finally {
            IoUtils.close(br);
            IoUtils.close(isr);
            IoUtils.close(is);
        }
    }
}
