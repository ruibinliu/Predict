package mo.edu.must.perdict.lazy.knnm2;

import java.util.ArrayList;

import mo.edu.must.perdict.lazy.knn2.Event;
import mo.edu.must.perdict.lazy.knn2.Instance;

public class KnnmCluster {
    Instance rep;
    ArrayList<Instance> num;
    Event cls;
    float sim;

    public KnnmCluster(Instance rep, ArrayList<Instance> num, Event cls, float sim) {
        this.rep = rep;
        this.num = num;
        this.cls = cls;
        this.sim = sim;
    }
}
