
package mo.edu.must.perdict.lazy.knn;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mo.edu.must.perdict.utils.Monitor;

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

    /**
     * 程序执行入口
     * 
     * @param args
     */
    public static void main(String[] args) {
        String datafile = "/Users/ruibin/workspace/adt/Predit/data/knn-data.txt";
        String testfile = "/Users/ruibin/workspace/adt/Predit/data/knn-test.txt";
        try {
            final List<List<Double>> datas = read(datafile);
            final List<List<Double>> testDatas = read(testfile);
            final Knn knn = new Knn(3, datas);
            int times = 0;
            long totalCost = 0;
            for (int i = 0; i < testDatas.size(); i++) {
                final List<Double> test = testDatas.get(i);

                System.out.print("测试元组: ");
                for (int j = 0; j < test.size(); j++) {
                    // System.out.print(test.get(j) + " ");
                }
                System.out.print("类别为: ");
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        int preditedClass = Math
                                .round(Float.parseFloat((knn.classifyInstance(test))));
                        System.out.println(preditedClass);
                    };
                };
                times++;
                totalCost += Monitor.run(r);
            }
            System.out.println("Cost " + totalCost + " ms, Test " + times + " times.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 从数据文件中读取数据
     * 
     * @param datas 存储数据的集合对象
     * @param path 数据文件的路径
     */
    public static List<List<Double>> read(String path) {
        List<List<Double>> datas = new ArrayList<List<Double>>();

        FileReader fr = null;
        BufferedReader br = null;
        try {
            fr = new FileReader(new File(path));
            br = new BufferedReader(fr);
            String data = br.readLine();
            List<Double> l = null;
            while (data != null) {
                String t[] = data.split(" ");
                l = new ArrayList<Double>();
                for (int i = 0; i < t.length; i++) {
                    l.add(Double.parseDouble(t[i]));
                }
                datas.add(l);
                data = br.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                }
            }
            if (fr != null) {
                try {
                    fr.close();
                } catch (IOException e) {
                }
            }
        }

        return datas;
    }
}
