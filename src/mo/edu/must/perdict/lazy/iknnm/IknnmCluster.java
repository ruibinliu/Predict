package mo.edu.must.perdict.lazy.iknnm;

import java.util.ArrayList;

/**
 * Created by HackerZ on 2016/12/24.
 */
public class IknnmCluster {
    iknnInstance req;
    ArrayList<iknnInstance> num;
    String cls;
    float sim;
    int lay;

    public IknnmCluster() {
//        this.req = req;
//        this.num = num;
//        this.cls = cls;
//        this.sim = sim;
//        this.lay = lay;
    }

//    public

    @Override
    public String toString()  {
        return "IKnnmCluster [ req:" + req + ", num:" + num+ ", cls:"
                + cls + ", sim:" + sim + ", lay:" + lay + " ]";
    }
}
