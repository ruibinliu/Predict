
package mo.edu.must.perdict.lazy.knn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import mo.edu.must.perdict.utils.FileUtils;
import mo.edu.must.perdict.utils.FileUtils.Listener;

/** KNN算法测试类 */
public class KnnMain {
    public static final int TOP_K = 5;
    private static final HashMap<String, HashMap<String, String>> SIMILARITY_MAP = new HashMap<>();

    /** 程序执行入口 */
    public static void main(String[] args) {
        final ArrayList<String> records = new ArrayList<>();
        FileUtils.read("out/tan-data.txt", new Listener() {
            @Override
            public void onReadLine(String line) {
                records.add(line);
            }
        });
        records.remove(0); // 第一行是字段的说明

        Dataset dataset = new Dataset();
        for (String line : records) {
            Instance instance = new Instance(line);
            dataset.add(instance);
        }

        int size = dataset.size();
        int foldCount = 10;
        int k = 100;
        int top = 5;
        int foldSize = size / foldCount;
        int testingSize = size / foldCount;
        int trainingSize = size - testingSize;
        initSimilarityMap();
        System.out.println("Size: " + size);
        System.out.println("Train: " + trainingSize);
        System.out.println("Test: " + testingSize);
        System.out.println("Count: " + trainingSize * testingSize);

        int[] totalMatched = new int[top];
        int[] totalUnmatched = new int[top];

        for (int i = 0; i < top; i++) {
            totalMatched[i] = 0;
            totalUnmatched[i] = 0;
        }

        for (int fold = 0; fold < foldCount; fold++) {
            int testIndexStart = fold * foldSize;
            int testIndexStop = testIndexStart + foldSize;

            Dataset testDataset = new Dataset();
            Dataset trainDataset = new Dataset();

            for (int i = 0; i < size; i++) {
                if (i >= testIndexStart && i < testIndexStop) {
                    testDataset.add(dataset.get(i));
                } else {
                    trainDataset.add(dataset.get(i));
                }
            }

            int[] matched = new int[top];
            int[] unmatched = new int[top];

            for (int i = 0; i < top; i++) {
                matched[i] = 0;
                unmatched[i] = 0;
            }

            long t0, t1;
            t0 = System.currentTimeMillis();
            for (int i = 0; i < testDataset.size(); i++) {
                final HashMap<Instance, Float> instanceDistance = new HashMap<>();
                Instance instance1 = testDataset.get(i);

                for (int j = 0; j < trainDataset.size(); j++) {
                    Instance instance2 = trainDataset.get(j);

                    float distance = computeDistance(instance1, instance2);
                    instanceDistance.put(instance2, distance);
                    // StringBuilder builder = new StringBuilder();
                    // builder.append("Instance1: " + instance1 + "\n");
                    // builder.append("Instance2: " + instance1 + "\n");
                    // builder.append("Distance: " +
                    // instance1.computeDistance(instance2));
                    // FileUtils.write("/Users/ruibin/logs/knn2.txt",
                    // builder.toString(), true);
                }
                Collections.sort(trainDataset, new Comparator<Instance>() {
                    @Override
                    public int compare(Instance o1, Instance o2) {
                        float distance1 = instanceDistance.get(o1);
                        float distance2 = instanceDistance.get(o2);
                        return (int)((distance1 * 1000000000) - (distance2 * 1000000000));
                    }
                });

                final HashMap<String, Integer> instanceCount = new HashMap<>();
                for (int n = 0; n < k; n++) {
                    Instance instance = trainDataset.get(n);

                    String key = instance.getApp();
                    if (instanceCount.containsKey(key)) {
                        instanceCount.put(key, instanceCount.get(key) + 1);
                    } else {
                        instanceCount.put(key, 1);
                    }
                }
                ArrayList<String> predictedDataset = new ArrayList<>();
                predictedDataset.addAll(instanceCount.keySet());
                Collections.sort(predictedDataset, new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        return instanceCount.get(o2) - instanceCount.get(o1);
                    }
                });

                for (int t = 0; t < top; t++) {
                    boolean isMatched = false;
                    for (int n = 0; n < (t + 1) && n < predictedDataset.size(); n++) {
                        String key = predictedDataset.get(n);
                        String actual = instance1.getApp();
                        if (actual.equals(key)) {
                            isMatched = true;
                            break;
                        }
                    }
                    if (isMatched) {
                        matched[t]++;
                    } else {
                        unmatched[t]++;
                    }
                }
            }

            for (int t = 0; t < top; t++) {
                int total = matched[t] + unmatched[t];
                float accuracy = (float)matched[t] / total;
                System.out.println("Predicted " + t + " apps, matched: " + matched[t]
                        + ", unmatched: " + unmatched[t] + ", total: " + total + ", accuracy: "
                        + accuracy);

                totalMatched[t] += matched[t];
                totalUnmatched[t] += unmatched[t];
            }

            t1 = System.currentTimeMillis();
            System.out.println("Cost " + (t1 - t0) + " ms");
        }

        for (int t = 0; t < top; t++) {
            int total = totalMatched[t] + totalUnmatched[t];
            float accuracy = (float)totalMatched[t] / total;
            System.out.println("Total: Predicted " + (t + 1) + " apps, matched: " + totalMatched[t]
                    + ", unmatched: " + totalUnmatched[t] + ", total: " + total + ", accuracy: "
                    + accuracy);
        }
    }

    public static void initSimilarityMap() {
        SIMILARITY_MAP.clear();
        FileUtils.read("out/similarity.txt", new Listener() {
            @Override
            public void onReadLine(String line) {
                String[] split = line.split(" ");
                if (split.length < 3) return;

                HashMap<String, String> map = SIMILARITY_MAP.get(split[0]);
                if (map == null) {
                    map = new HashMap<>();
                    SIMILARITY_MAP.put(split[0], map);
                }
                map.put(split[1], split[2]);
            }
        });
    }

    public static float computeDistance(Instance inst1, Instance inst2) {
        float similarity1 = getSimilarity(inst1.getLastApp(), inst2.getLastApp());
        float similarity2 = getSimilarity(inst1.getLastBluetooth(), inst2.getLastBluetooth());
        float similarity3 = getSimilarity(inst1.getLastWifi(), inst2.getLastWifi());
        float similarity4 = getSimilarity(inst1.getLastAudio(), inst2.getLastAudio());
        float similarity5 = getSimilarity(inst1.getLastLight(), inst2.getLastLight());
        float similarity6 = getSimilarity(inst1.getLastLocation(), inst2.getLastLocation());
        float similarity7 = getSimilarity(inst1.getLastCharge(), inst2.getLastCharge());
        float similarity8 = getSimilarity(inst1.getLastData(), inst2.getLastData());

        float distance = (float) Math.sqrt(
                Math.pow(similarity1, 2) +
                Math.pow(similarity2, 2) +
                Math.pow(similarity3, 2) +
                Math.pow(similarity4, 2) +
                Math.pow(similarity5, 2) +
                Math.pow(similarity6, 2) +
                Math.pow(similarity7, 2) +
                Math.pow(similarity8, 2)
                );

        return distance;
    }

    private static float getSimilarity(String key1, String key2) {
        HashMap<String, String> map = SIMILARITY_MAP.get(key2);
        if (map == null) {
            map = new HashMap<>();
            SIMILARITY_MAP.put(key1, map);
        }
        return parseFloat(map.get(key2), 0.0f);
    }

    public static float parseFloat(String string, float defaultValue) {
        try {
            return Float.valueOf(string);
        } catch (Exception e) {
            return defaultValue;
        }
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
