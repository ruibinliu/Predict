
package mo.edu.must.perdict.lazy.knn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

/**
 * KNN算法主体类
 * 
 * @author Rowen
 * @qq 443773264
 * @mail luowen3405@163.com
 * @blog blog.csdn.net/luowen3405
 * @data 2011.03.25
 */
public class Knn {
    private List<List<Double>> datas;

    private int k;

    public Knn() {
        this.k = 1;
        this.datas = new ArrayList<List<Double>>();
    }

    public Knn(int k, List<List<Double>> datas) {
        this.k = k;
        this.datas = datas;
    }

    /**
     * 设置优先级队列的比较函数，距离越大，优先级越高
     */
    private Comparator<KnnNode> comparator = new Comparator<KnnNode>() {
        public int compare(KnnNode o1, KnnNode o2) {
            if (o1.getDistance() >= o2.getDistance()) {
                return 1;
            } else {
                return 0;
            }
        }
    };

    /**
     * 获取K个不同的随机数
     * 
     * @param k 随机数的个数
     * @param max 随机数最大的范围
     * @return 生成的随机数数组
     */
    public List<Integer> getRandKNum(int k, int max) {
        List<Integer> rand = new ArrayList<Integer>(k);
        for (int i = 0; i < k; i++) {
            int temp = (int)(Math.random() * max);
            if (!rand.contains(temp)) {
                rand.add(temp);
            } else {
                i--;
            }
        }
        return rand;
    }

    /**
     * 计算测试元组与训练元组之前的距离
     * 
     * @param d1 测试元组
     * @param d2 训练元组
     * @return 距离值
     */
    public double calDistance(List<Double> d1, List<Double> d2) {
        double distance = 0.00;
        for (int i = 0; i < d1.size(); i++) {
            distance += (d1.get(i) - d2.get(i)) * (d1.get(i) - d2.get(i));
        }
        return distance;
    }

    /**
     * 执行KNN算法，获取测试元组的类别
     * 
     * @param datas 训练数据集
     * @param testData 测试元组
     * @param k 设定的K值
     * @return 测试元组的类别
     */
    public String[] classifyInstance(List<Double> testData) {
        PriorityQueue<KnnNode> pq = new PriorityQueue<KnnNode>(k, comparator);
        List<Integer> randNum = getRandKNum(k, datas.size());
        for (int i = 0; i < k; i++) {
            int index = randNum.get(i);
            List<Double> currData = datas.get(index);
            String c = currData.get(currData.size() - 1).toString();
            KnnNode node = new KnnNode(index, calDistance(testData, currData), c);
            pq.add(node);
        }
        for (int i = 0; i < datas.size(); i++) {
            List<Double> t = datas.get(i);
            double distance = calDistance(testData, t);
            KnnNode top = pq.peek();
            if (top.getDistance() > distance) {
                pq.remove();
                pq.add(new KnnNode(i, distance, t.get(t.size() - 1).toString()));
            }
        }

        return getMostClass(pq);
    }

    /**
     * 获取所得到的k个最近邻元组的多数类
     * 
     * @param pq 存储k个最近近邻元组的优先级队列
     * @return 多数类的名称
     */
    private String[] getMostClass(PriorityQueue<KnnNode> pq) {
        // Count
        final HashMap<String, Integer> classCount = new HashMap<>();
        for (int i = 0; i < pq.size(); i++) {
            KnnNode node = pq.remove();
            String c = node.getClazz();
            if (classCount.containsKey(c)) {
                classCount.put(c, classCount.get(c) + 1);
            } else {
                classCount.put(c, 1);
            }
        }
        ArrayList<String> sorted = new ArrayList<>();
        sorted.addAll(classCount.keySet());
        Collections.sort(sorted, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return classCount.get(o2) - classCount.get(o1);
            }
        });
//        for (int i = 0; i < sorted.size(); i++) {
//            String c = sorted.get(i);
//            System.out.println("Class: " + c + ", Count: " + classCount.get(c));
//        }

        String[] topClass = new String[KnnMain.TOP_K];
        for (int i = 0; i < topClass.length; i++) {
            if (sorted.size() > i) {
                topClass[i] = sorted.get(i);
            } else {
                topClass[i] = "";
            }
        }
        return topClass;

        // Find Max
//        int maxIndex = -1;
//        int maxCount = 0;
//        Object[] classes = classCount.keySet().toArray();
//        for (int i = 0; i < classes.length; i++) {
//            if (classCount.get(classes[i]) > maxCount) {
//                maxIndex = i;
//                maxCount = classCount.get(classes[i]);
//            }
//        }
//        return classes[maxIndex].toString();
    }
}
