
package mo.edu.must.perdict.lazy.knn;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

public class Preprocess {
    public static void main(String[] args) {
        HashMap<String, String[]> vectorMap = getVectorMap();

        for (Entry<String, String[]> entry : vectorMap.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }

    public static HashMap<String, String[]> getVectorMap() {
        File vectorMapFile = new File(
                "/Users/ruibin/workspace/adt/Predit/src/com/must/perdit/knn/vector_map.txt");

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
            close(br);
            close(fr);
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
