
package mo.edu.must.perdict.lazy.knn;

/**
 * KNN结点类，用来存储最近邻的k个元组相关的信息
 * 
 * @author Rowen
 * @qq 443773264
 * @mail luowen3405@163.com
 * @blog blog.csdn.net/luowen3405
 * @data 2011.03.25
 */
public class KnnNode {
    private int index; // 元组标号

    private double distance; // 与测试元组的距离

    private String cls; // 所属类别

    public KnnNode(int index, double distance, String c) {
        this.index = index;
        this.distance = distance;
        this.cls = c;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getClazz() {
        return cls;
    }
}
