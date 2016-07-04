
package mo.edu.must.perdict.preprocess;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import mo.edu.must.perdict.utils.IoUtils;

public class ArffFile {

    private static final String RELATION_PREFIX = "@relation";
    private static final String ATTRIBUTE_PREFIX = "@attribute";
    private static final String DATA_PREFIX = "@data";

    private String relation;

    private ArrayList<String> attributeList = new ArrayList<>();

    private ArrayList<Data> dataList = new ArrayList<>();

    public static ArffFile fromFile(String filePath) {
        ArffFile arffFile = new ArffFile();

        FileInputStream input = null;
        InputStreamReader inReader = null;
        BufferedReader reader = null;
        try {
            input = new FileInputStream(filePath);
            inReader = new InputStreamReader(input);
            reader = new BufferedReader(inReader);

            String line;
            boolean isData = false;
            while ((line = reader.readLine()) != null) {
                if (line.trim().length() == 0) {
                    continue;
                } else if (line.startsWith(RELATION_PREFIX)) {
                    arffFile.relation = line.substring(RELATION_PREFIX.length());
                } else if (line.startsWith(ATTRIBUTE_PREFIX)) {
                    arffFile.attributeList.add(line.split(" ")[1]);
                } else if (line.startsWith(DATA_PREFIX)) {
                    isData = true;
                } else if (isData) {
                    Data data = new Data();
                    String[] split = line.split(",");
                    for (int i = 0; i < split.length; i ++) {
                        String value = split[i];
                        data.put(arffFile.attributeList.get(i), value);
                    }
                    arffFile.dataList.add(data);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IoUtils.close(reader);
            IoUtils.close(inReader);
            IoUtils.close(input);
        }

        return arffFile;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public ArrayList<String> getAttributeList() {
        return attributeList;
    }

    public void setAttributeList(ArrayList<String> attributeList) {
        this.attributeList = attributeList;
    }


    public ArrayList<Data> getDataList() {
        return dataList;
    }


    public void setDataList(ArrayList<Data> dataList) {
        this.dataList = dataList;
    }


    @Override
    public String toString() {
        return "ArffFile [relation=" + relation + ", attributeList=" + attributeList
                + ", dataList=" + dataList + "]";
    }

    public static class Data extends HashMap<String, String> {
        private static final long serialVersionUID = 1L;
    }
}
