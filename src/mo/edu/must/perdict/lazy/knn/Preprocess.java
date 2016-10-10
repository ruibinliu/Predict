
package mo.edu.must.perdict.lazy.knn;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import mo.edu.must.perdict.utils.FileUtils;
import mo.edu.must.perdict.utils.FileUtils.Listener;

public class Preprocess {
    public static void main(String[] args) {
        HashMap<String, String[]> vectorMap = getVectorMap();

        for (Entry<String, String[]> entry : vectorMap.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }

    public static HashMap<String, String[]> getVectorMap() {
        String filePath = "out/vector.txt";

        final HashMap<String, String[]> vectorMap = new HashMap<>();

        // 根据Session Feature，用不同feature的词向量组成一个长的词向量
        FileUtils.read(filePath, new Listener() {
            @Override
            public void onReadLine(String line) {
                int index = line.indexOf(" ");
                String event;
                String vector;
                if (index == -1) {
                    event = line;
                    vector = "";
                } else {
                    event = line.substring(0, index).trim();
                    vector = line.substring(index).trim();
                }
                if ("".equals(vector)) {
                    return;
                }
                while(vector.indexOf("  ") >= 0) {
                    vector = vector.replace("  ", " ");
                }
                vector = vector.replace("[", "").replace("]", "").trim();
                String[] split = vector.split(" ");
                vectorMap.put(event, split);
            }
        });

        for (Entry<String, String[]> entry : vectorMap.entrySet()) {
            System.out.println(entry.getKey() + ", " + entry.getValue());
        }

        return vectorMap;
    }

    private static void close(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
            }
        }
    }
}
