
package mo.edu.must.perdict.lazy.knnm;

import java.util.List;

public class Instance {
    private int cls;

    private List<Double> vector;

    private boolean isCovered;

    public Instance(int cls, List<Double> vector) {
        this.cls = cls;
        this.vector = vector;
    }

    public Double distance(Instance instance) {
        double distance = 0.00;

        for (int i = 0; i < vector.size(); i++) {
            distance += (vector.get(i) - instance.vector.get(i))
                    * (vector.get(i) - instance.vector.get(i));
        }
        return distance;
    }

    public int getCls() {
        return cls;
    }

    public void setCls(int cls) {
        this.cls = cls;
    }

    public boolean isCovered() {
        return isCovered;
    }

    public void setCovered(boolean isCovered) {
        this.isCovered = isCovered;
    }
}
