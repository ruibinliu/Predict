
package mo.edu.must.perdict.lazy.knnm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class Knnm {
    private HashMap<Instance, HashMap<Instance, Double>> distanceCache;

    public void buildClassifier(List<Instance> instances) {
        System.out.println("Calculating distances...");
        distanceCache = new HashMap<>();
        for (int i = 0, size = instances.size(); i < size; i++) {
            Instance instance = instances.get(i);
            HashMap<Instance, Double> distanceMap = distanceCache.get(instance);
            if (distanceMap == null) {
                distanceMap = new HashMap<>();
                distanceCache.put(instance, distanceMap);
            }
            for (Instance inst : instances) {
                if (inst != instance) {
                    double distance = instance.distance(inst);
                    distanceMap.put(inst, distance);
                }
            }
        }
        System.out.println("Done calculating distances.");

        boolean isAllCovered;
        do {
            run(instances);
            isAllCovered = true;
            for (Instance i : instances) {
                if (!i.isCovered()) {
                    isAllCovered = false;
                    break;
                }
            }
        } while(!isAllCovered);
    }

    private void run(List<Instance> instances) {
        ArrayList<ArrayList<Instance>> clusterList = new ArrayList<>();

        for (int i = 0, size = instances.size(); i < size; i++) {
            Instance instance = instances.get(i);
            if (!instance.isCovered()) {
                final HashMap<Instance, Double> distanceMap = distanceCache.get(instance);

                ArrayList<Instance> sorted = new ArrayList<>();
                for (Instance inst : distanceMap.keySet()) {
                    if (!inst.isCovered()) {
                        sorted.add(inst);
                    }
                }
                Collections.sort(sorted, new Comparator<Instance>() {

                    @Override
                    public int compare(Instance o1, Instance o2) {
                        int d1 = (int)(distanceMap.get(o1) * 100000000);
                        int d2 = (int)(distanceMap.get(o2) * 100000000);
                        return d1 - d2;
                    }
                });
                ArrayList<Instance> matched = new ArrayList<>();
                for (Instance inst : sorted) {
                    if (inst.getCls() == instance.getCls()) {
                        matched.add(inst);
                    } else {
                        break;
                    }
                }
                if (matched.size() > 0) {
                    clusterList.add(matched);
                }
            }
        }

        ArrayList<Instance> max = null;

        for (ArrayList<Instance> cluster : clusterList) {
            if (max == null || cluster.size() > max.size()) {
                max = cluster;
            }
        }
        for (Instance inst : max) {
            inst.setCovered(true);
            System.out.println(inst.getCls());
        }
        System.out.println("-----");
    }
}
