
package mo.edu.must.perdict.lazy.knnm;

import java.util.ArrayList;
import java.util.List;

import mo.edu.must.perdict.lazy.knn.KnnMain;

public class KnnmMain {

    public static void main(String[] args) {
        String datafile = "/Users/ruibin/workspace/adt/Predit/data/knn-data.txt";
        String testfile = "/Users/ruibin/workspace/adt/Predit/data/knn-test.txt";
        try {
            final List<List<Double>> dataList = KnnMain.read(datafile);
            final List<List<Double>> testDatas = KnnMain.read(testfile);

            List<Instance> instances = new ArrayList<>();
            for (List<Double> data : dataList) {
                int cls = (int)data.get(data.size() - 1).doubleValue();
                data.remove(data.size() - 1);
                instances.add(new Instance(cls, data));
            }

            Knnm knnm = new Knnm();
            knnm.buildClassifier(instances);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
