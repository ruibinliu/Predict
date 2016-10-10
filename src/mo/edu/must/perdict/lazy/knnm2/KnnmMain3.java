
package mo.edu.must.perdict.lazy.knnm2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import mo.edu.must.perdict.lazy.knn.Dataset;
import mo.edu.must.perdict.lazy.knn.Instance;
import mo.edu.must.perdict.lazy.knn.KnnMain;
import mo.edu.must.perdict.utils.FileUtils;
import mo.edu.must.perdict.utils.FileUtils.Listener;

public class KnnmMain3 {
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
        int top = 5;
        int foldSize = size / foldCount;
        int testingSize = size / foldCount;
        int trainingSize = size - testingSize;
        KnnMain.initSimilarityMap();
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

            final Dataset testDataset = new Dataset();
            final Dataset trainDataset = new Dataset();

            for (int i = 0; i < size; i++) {
                if (i >= testIndexStart && i < testIndexStop) {
                    testDataset.add(dataset.get(i));
                } else {
                    trainDataset.add(dataset.get(i));
                }
            }

            long t0, t1;
            t0 = System.currentTimeMillis();
            final HashMap<Instance, HashMap<Instance, Float>> instanceDistance = new HashMap<>();
            for (int i = 0; i < trainDataset.size(); i++) {
                Instance inst1 = trainDataset.get(i);

                HashMap<Instance, Float> map = instanceDistance.get(inst1);
                if (map == null) {
                    map = new HashMap<>();
                    instanceDistance.put(inst1, map);
                }
                for (Instance inst2 : trainDataset) {
                    if (inst1 == inst2) continue;

                    float distance;

                    if (map.containsKey(inst2)) {
                        distance = map.get(inst2);
                    } else {
                        distance = KnnMain.computeDistance(inst1, inst2);
                        map.put(inst2, distance);
                    }
                }
            }
            t1 = System.currentTimeMillis();
            System.out.println("Computing cost " + (t1 - t0) + " ms");

            Knnm knnm = new Knnm();
            HashSet<Instance> instanceCovered = new HashSet<>();
            while(instanceCovered.size() < trainDataset.size()) {
                System.out.println("Covered/Total: " + instanceCovered.size() + "/" + trainDataset.size());
                ArrayList<KnnmCluster> clusterList = new ArrayList<>();

                for (Instance instance1 : trainDataset) {
                    KnnmCluster cluster = new KnnmCluster(instance1, new ArrayList<Instance>(), instance1.getApp(), 0);
                    clusterList.add(cluster);
                    cluster.num.add(instance1);

                    if (instanceCovered.contains(instance1)) continue;

                    final HashMap<Instance, Float> instanceByDistance = new HashMap<>();
                    for (Instance instance2 : trainDataset) {
                        if (instance1 == instance2) continue;
                        if (instanceCovered.contains(instance2)) continue;

                        float distance = instanceDistance.get(instance1).get(instance2);
                        instanceByDistance.put(instance2, distance);
                    }
                    ArrayList<Instance> instancesNearBy1 = new ArrayList<>();
                    instancesNearBy1.addAll(instanceByDistance.keySet());
                    Collections.sort(instancesNearBy1, new Comparator<Instance>() {
                        @Override
                        public int compare(Instance o1, Instance o2) {
                            float distance1 = instanceByDistance.get(o1);
                            float distance2 = instanceByDistance.get(o2);
                            return (int)((distance1 * 1000000000) - (distance2 * 1000000000));
                        }
                    });
                    for (Instance instance : instancesNearBy1) {
                        if (instance1.getApp().equals(instance.getApp())) {
                            float distance = instanceByDistance.get(instance);
                            cluster.num.add(instance);
                            cluster.sim = distance;
                        } else {
                            break;
                        }
                    }
                }

                Collections.sort(clusterList, new Comparator<KnnmCluster>() {
                    @Override
                    public int compare(KnnmCluster o1, KnnmCluster o2) {
                        return o2.num.size() - o1.num.size();
                    }
                });

//                for (KnnmCluster cluster : clusterList) {
//                    System.out.println(cluster);
//                }

                KnnmCluster cluster = clusterList.get(0);
                System.out.println("cluster.num.size(): " + cluster.num.size());
                if (cluster.num.size() < 2) {
                    break;
                }
                knnm.add(cluster);
                for (Instance instance : cluster.num) {
                    instanceCovered.add(instance);
                }
            }

            for (KnnmCluster cluster : knnm) {
                System.out.println(String.format("Cluster: <Cls=%s, Sim=%s, Num=%s, Rep=%s>",
                        cluster.cls.toString(),
                        cluster.sim,
                        cluster.num.size(),
                        cluster.rep.toString()));
            }

            // Test
            for (Instance instance1 : testDataset) {
                final HashMap<KnnmCluster, Float> clusterDistance = new HashMap<>();
                for (KnnmCluster cluster : knnm) {

                    if (instance1 == cluster.rep) {
                        clusterDistance.put(cluster, 0f);
                    } else {
                        float distance = KnnMain.computeDistance(instance1, cluster.rep);
                        if (distance <= cluster.sim) {
                            clusterDistance.put(cluster, 0f);
                        } else {
                            clusterDistance.put(cluster, distance - cluster.sim);
                        }
                    }
                }
                ArrayList<KnnmCluster> nearestClusters = new ArrayList<>();
                nearestClusters.addAll(clusterDistance.keySet());
                Collections.sort(nearestClusters, new Comparator<KnnmCluster>() {
                    @Override
                    public int compare(KnnmCluster o1, KnnmCluster o2) {
                        float distance1 = clusterDistance.get(o1);
                        float distance2 = clusterDistance.get(o2);
                        return (int)((distance1 * 1000000000) - (distance2 * 1000000000));
                    }
                });
                for (KnnmCluster cluster : nearestClusters) {
                    System.out.println(cluster.cls.toString() + ": " + clusterDistance.get(cluster));
                }

                // Test
                int[] matched = new int[top];
                int[] unmatched = new int[top];

                for (int i = 0; i < top; i++) {
                    matched[i] = 0;
                    unmatched[i] = 0;
                }

                for (int t = 0; t < top; t++) {
                    boolean isMatched = false;
                    for (int n = 0; n < (t + 1) && n < nearestClusters.size(); n++) {
                        KnnmCluster nearestCluster = nearestClusters.get(n);
                        if (instance1.getApp().equals(nearestCluster.cls)) {
                            isMatched = true;
                        }
                    }
                    if (isMatched) {
                        matched[t]++;
                    } else {
                        unmatched[t]++;
                    }
                }

                // Print the result for this fold.
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
        }

        for (int t = 0; t < top; t++) {
            int total = totalMatched[t] + totalUnmatched[t];
            float accuracy = (float)totalMatched[t] / total;
            System.out.println("Total: Predicted " + (t + 1) + " apps, matched: " + totalMatched[t]
                    + ", unmatched: " + totalUnmatched[t] + ", total: " + total + ", accuracy: "
                    + accuracy);
        }
    }
}
