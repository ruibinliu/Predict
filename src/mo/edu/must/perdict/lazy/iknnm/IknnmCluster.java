package mo.edu.must.perdict.lazy.iknnm;

import mo.edu.must.perdict.lazy.knn.Instance;

import java.util.ArrayList;

/**
 * Created by HackerZ on 2016/12/24.
 */
public class IknnmCluster {
    Instance req;
    ArrayList<Instance> num;
    String cls;
    float sim;
    int lay;

    public IknnmCluster(Instance req, ArrayList<Instance> num, String cls, float sim, int lay) {
        this.req = req;
        this.num = num;
        this.cls = cls;
        this.sim = sim;
        this.lay = lay;
    }

    @Override
    public String toString()  {
        return "IKnnmCluster [ req:" + req + ", num:" + num+ ", cls:"
                + cls + ", sim:" + sim + ", lay:" + lay + " ]";
    }
}
