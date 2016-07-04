
package mo.edu.must.perdict.lazy.knnm;

public class KnnmCluster {
    /**
     * 该区域中数据点的类别
     */
    private int cls;

    /**
     * 该区域的半径, 即最远点到圆心的距离
     */
    private double sim;

    /**
     * 该区域覆盖点的数量
     */
    private int num;

    /**
     * 圆心本身
     */
    private Instance rep;

}
