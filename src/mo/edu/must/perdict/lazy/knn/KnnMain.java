
package mo.edu.must.perdict.lazy.knn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mo.edu.must.perdict.utils.FileUtils;
import mo.edu.must.perdict.utils.FileUtils.Listener;

/**
 * KNN算法测试类
 * 
 * @author Rowen
 * @qq 443773264
 * @mail luowen3405@163.com
 * @blog blog.csdn.net/luowen3405
 * @data 2011.03.25
 */
public class KnnMain {
    private static final int K = 100;
    public static final int TOP_K = 5;

    /**
     * 程序执行入口
     * 
     * @param args
     */
    public static void main(String[] args) {
        final ArrayList<String> records = new ArrayList<>();
        FileUtils.read("out/tan-data.txt", new Listener() {
            @Override
            public void onReadLine(String line) {
                records.add(line);
            }
        });
        records.remove(0); // 第一行是字段的说明

        HashMap<String, String[]> vectorMap = Preprocess.getVectorMap();
        StringBuilder builder = new StringBuilder();
        HashMap<String, Integer> classMap = new HashMap<>();
        for (String line : records) {
            String vec = "";
            String[] split = line.split(" ");
            boolean isSkip = false;
            for (int i = 0; i < split.length - 1; i++) {
                String[] vector = vectorMap.get(split[i]);
                if ("null".equals(split[i])) {
                    vector = new String[50];
                    for (int k = 0; k < vector.length; k++) {
                        vector[k] = "0";
                    }
                } else if (vector == null || vector.length == 0 || "".equals(vector[0])) {
                    // 有一些词没有对应的词向量
                    isSkip = true;
                    break;
                }
                for (int j = 0; j < vector.length; j++) {
                    vec += (vector[j] + " ");
                }
            }
            if (!isSkip) {
                String clazz = split[split.length - 1];
                if (!classMap.containsKey(clazz)) {
                    classMap.put(clazz, classMap.size());
                }
                int id = classMap.get(clazz);
                builder.append(vec + id + "\n");
            }
        }
        for (String clazz : classMap.keySet()) {
            System.out.println(clazz + ": " + classMap.get(clazz));
        }
        FileUtils.write("out/knn-data.txt", builder.toString());

        String datafile = "out/knn-data.txt";
        String testfile = "out/knn-data.txt";
        final List<List<Double>> datas = read(datafile);
        final List<List<Double>> testDatas = read(testfile);
        final Knn knn = new Knn(K, datas);
        int times = 0;
        long totalCost = 0;
        int[] totalMatched = new int[TOP_K];
        int[] totalUnmatched = new int[TOP_K];
        for (int i = 0; i < totalMatched.length; i++) {
        	totalMatched[i] = 0;
        	totalUnmatched[i] = 0;
        }

        for (int c = 0; c < TOP_K; c++) {
            for (int i = 0; i < testDatas.size(); i++) {
                final List<Double> test = testDatas.get(i);

//                System.out.println("=== 测试元组 " + i + " ===");
                // for (int j = 0; j < test.size(); j++) {
                // System.out.print(test.get(j) + " ");
                // }

                long t0, t1;
                t0 = System.currentTimeMillis();
                // System.out.println("类别为: " + preditedClass);
                for (int j = 0; j < datas.size(); j++) {
                    int matched = 0;
                    int unmatched = 0;
                    if (j == i)
                        continue;
                    // 在测试数据中寻找相同Context的记录，然后做比较
                    List<Double> t = datas.get(j);
                    if (isSamePrecondition(test, t)) {
                        boolean isMatched = false;
                        String[] topClasses = knn.classifyInstance(test);
                        for (int k = 0; k <= c; k++) {
                            if ("".equals(topClasses[k]))
                                continue;
                            int preditedClass = Math.round(Float.parseFloat((topClasses[k])));
                            if (preditedClass == t.get(t.size() - 1)) {
                                isMatched = true;
                                break;
                            }
                        }
                        if (isMatched) {
                            matched++;
                        } else {
                            unmatched++;
                        }
                    }
                    totalMatched[c] += matched;
                    totalUnmatched[c] += unmatched;
//                    int total = matched + unmatched;
//                    double accuracy = total == 0 ? 0 : (double)((double)matched / total);
//                    System.out.println("matched: " + matched + ", unmatched: " + unmatched
//                            + ", Accuracy: " + accuracy + " (" + matched + "/" + total + ")");
                }
                t1 = System.currentTimeMillis();
                times++;
                totalCost += (t1 - t0);
            }
            System.out.println("===== Predited app numbers: " + (c + 1) + " =====");
            System.out.println("totalMatched: " + totalMatched[c] + ", totalUnmatched: "
                    + totalUnmatched[c] + ", total: " + (totalMatched[c] + totalUnmatched[c]) + ", totalAccuracy: "
                    + ((double)totalMatched[c] / (totalMatched[c] + totalUnmatched[c])));
            System.out.println("==========");
        }
        System.out.println("Cost " + totalCost + " ms, Test " + times + " times, Average "
                + totalCost / times + " ms.");
    }

    private static final boolean isSamePrecondition(List<Double> list1, List<Double> list2) {
        for (int k = 0; k < list1.size() - 1; k++) { // 在数据文件中，列表的最后一个，是分类
            if (list1.get(k).doubleValue() != list2.get(k).doubleValue()) {
                return false;
            }
        }

        return true;
    }

    /**
     * 从数据文件中读取数据
     * 
     * @param datas 存储数据的集合对象
     * @param path 数据文件的路径
     */
    public static List<List<Double>> read(String path) {
        final List<List<Double>> datas = new ArrayList<List<Double>>();

        FileUtils.read(path, new Listener() {
            @Override
            public void onReadLine(String line) {
                String t[] = line.split(" ");
                List<Double> l = new ArrayList<Double>();
                for (int i = 0; i < t.length; i++) {
                    l.add(Double.parseDouble(t[i]));
                }
                datas.add(l);
            }
        });

        return datas;
    }
}
