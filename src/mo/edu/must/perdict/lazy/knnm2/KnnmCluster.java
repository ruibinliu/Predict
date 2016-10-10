package mo.edu.must.perdict.lazy.knnm2;

import java.util.ArrayList;

import mo.edu.must.perdict.lazy.knn.Instance;

public class KnnmCluster {
    Instance rep;
    ArrayList<Instance> num;
    String cls;
    float sim;

    public KnnmCluster(Instance rep, ArrayList<Instance> num, String cls, float sim) {
        this.rep = rep;
        this.num = num;
        this.cls = cls;
        this.sim = sim;
    }

    @Override
    public String toString() {
        return "KnnmCluster [rep=" + rep + ", num=" + num + ", cls=" + cls + ", sim=" + sim + "]";
    }
}
