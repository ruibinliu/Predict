
package mo.edu.must.perdict.lazy.knn2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import mo.edu.must.perdict.utils.FileUtils;
import mo.edu.must.perdict.utils.FileUtils.Listener;

public class KnnMain2 {
    public static void main(String[] args) {
        final ArrayList<String> records = new ArrayList<>();
        FileUtils.read("data/mdc-data-out.txt", new Listener() {
            @Override
            public void onReadLine(String line) {
                records.add(line);
            }
        });
        Dataset dataset = new Dataset(records);
        HashMap<String, Integer> instances = new HashMap<>();
        for (Instance instance : dataset) {
            String key = instance.getEvent().toString();
            if (instances.containsKey(key)) {
                instances.put(key, instances.get(key) + 1);
            } else {
                instances.put(key, 1);
            }
        }
        for (String event : instances.keySet()) {
            System.out.println(instances.get(event) + ", " + event);
        }
        System.out.println("Event size: " + instances.keySet().size());

        int size = dataset.size();
        int foldCount = 10;
        int k = 100;
        int top = 5;
        int foldSize = size / foldCount;
        int testingSize = size / foldCount;
        int trainingSize = size - testingSize;
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

                    float distance = instance1.computeDistance(instance2);
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

                    String key = instance.getEvent().toString();
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
                        String actual = instance1.getEvent().toString();
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
}
