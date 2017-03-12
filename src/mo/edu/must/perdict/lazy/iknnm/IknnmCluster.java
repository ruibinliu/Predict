package mo.edu.must.perdict.lazy.iknnm;

import java.util.ArrayList;

/**
 * Created by HackerZ on 2016/12/24.
 */
public class IknnmCluster {
    iknnInstance rep;
    ArrayList<iknnInstance> num;
    String cls;
    Double sim;
    int lay;

    public IknnmCluster(iknnInstance rep, ArrayList<iknnInstance> num, String cls, double sim, int lay) {
        this.rep = rep;
        this.num = num;
        this.cls = cls;
        this.sim = sim;
        this.lay = lay;
    }

//    public

    @Override
    public String toString()  {
        return "IKnnmCluster [ rep:" + rep + ", num:" + num+ ", cls:"
                + cls + ", sim:" + sim + ", lay:" + lay + " ]";
    }
}
