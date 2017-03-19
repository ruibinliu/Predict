package mo.edu.must.perdict.lazy.iknnm;

//import mo.edu.must.perdict.lazy.knn.Dataset;
//import mo.edu.must.perdict.lazy.knn.Instance;
//import mo.edu.must.perdict.lazy.knn.KnnMain;
import mo.edu.must.perdict.lazy.knn.Instance;
import mo.edu.must.perdict.lazy.knnm2.Knnm;
import mo.edu.must.perdict.lazy.knnm2.KnnmCluster;
import mo.edu.must.perdict.utils.FileUtils;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by HackerZ on 2016/12/24.
 */
public class IknnmMain {
    public static void main() {
        final ArrayList<String> records = new ArrayList<>();
        FileUtils.read("out/new_vector.txt", new FileUtils.Listener() {
            @Override
            public void onReadLine(String line) {
                records.add(line);
            }
        });
        // records.remove(0); // 第一行是字段的说明

        Dataset dataset = new Dataset();
        for (String line : records) {
            iknnInstance instance = new iknnInstance(line);
            dataset.add(instance);
        }

        int size = dataset.size();
        int foldCount = 10;
        int top = 5;
        int foldSize = size / foldCount;
        int testingSize = size / foldCount;
        int trainingSize = size - testingSize;
//        KnnMain.initSimilarityMap();
        System.out.println("Size: " + size);
        System.out.println("Train: " + trainingSize);
        System.out.println("Test: " + testingSize);
        System.out.println("Count: " + trainingSize * testingSize);

        int[] totalMatched = new int[top]; // 输出5个预测结果
        int[] totalUnmatched = new int[top];

        // for search match or unmatch node
        for (int i = 0; i < top; i++) {
            totalMatched[i] = 0;
            totalUnmatched[i] = 0;
        }

        for (int fold = 0; fold< foldCount; fold++) {
            int testStartIndex = fold * foldSize;
            int testEndIndex= testStartIndex + foldSize;

            final Dataset testDataSet = new Dataset();
            final Dataset trainDataSet = new Dataset();

            for (int i= 0; i< size; i++) {
                if (i >= testStartIndex && i < testEndIndex) {
                    testDataSet.add(dataset.get(i));
                }else {
                    trainDataSet.add(dataset.get(i));
                }
            }

            long startBuildTime, endBuildTime;
            startBuildTime = System.currentTimeMillis();

            final HashMap<iknnInstance, HashMap<iknnInstance, Double>> instanceDistance = new HashMap<>();
            for (int i = 0; i < trainDataSet.size(); i++) {
                iknnInstance inst1 = trainDataSet.get(i);

                HashMap<iknnInstance, Double> map = instanceDistance.get(inst1);
                if (map == null) {
                    map = new HashMap<>();
                    instanceDistance.put(inst1, map);
                }
                for (iknnInstance inst2 : trainDataSet) {
                    if (inst1 == inst2) continue;

                    double distance;

                    if (map.containsKey(inst2)) {
                        distance = map.get(inst2);
                    } else {
                        distance = computeDistance(inst1.getVector(), inst2.getVector());
                        map.put(inst2, distance);
                    }
                }
            }
            endBuildTime = System.currentTimeMillis();
            System.out.println("Calculate eight last instance cost:" + (endBuildTime - startBuildTime) + " ms");

            Iknnm iknnm = new Iknnm();
            HashSet<iknnInstance> instanceCovered = new HashSet<>();
            while(instanceCovered.size() < trainDataSet.size()) {
                System.out.println("Covered/Total: " + instanceCovered.size() + "/" + trainDataSet.size());
                ArrayList<IknnmCluster> clusterList = new ArrayList<>();

                for (iknnInstance instance1 : trainDataSet) {
                    IknnmCluster cluster = new IknnmCluster(instance1, new ArrayList<iknnInstance>(), instance1.getApp(), 0, 0);
                    clusterList.add(cluster);
                    cluster.num.add(instance1);

                    if (instanceCovered.contains(instance1)) continue;

                    final HashMap<iknnInstance, Double> instanceByDistance = new HashMap<>();
                    for (iknnInstance instance2 : trainDataSet) {
                        if (instance1 == instance2) continue;
                        if (instanceCovered.contains(instance2)) continue;

                        double distance = instanceDistance.get(instance1).get(instance2);
                        instanceByDistance.put(instance2, distance);
                    }
                    ArrayList<iknnInstance> instancesNearBy1 = new ArrayList<>();
                    instancesNearBy1.addAll(instanceByDistance.keySet());
                    Collections.sort(instancesNearBy1, new Comparator<iknnInstance>() {
                        @Override
                        public int compare(iknnInstance o1, iknnInstance o2) {
                            double distance1 = instanceByDistance.get(o1);
                            double distance2 = instanceByDistance.get(o2);
                            return (int)((distance1 * 1000000000) - (distance2 * 1000000000));
                        }
                    });
                    for (iknnInstance instance : instancesNearBy1) {
                        if (instance1.getApp().equals(instance.getApp())) {
                            double distance = instanceByDistance.get(instance);
                            cluster.num.add(instance);
                            cluster.sim = distance;
                        } else {
                            break;
                        }
                    }
                }

                Collections.sort(clusterList, new Comparator<IknnmCluster>() {
                    @Override
                    public int compare(IknnmCluster o1, IknnmCluster o2) {
                        return o2.num.size() - o1.num.size();
                    }
                });

//                for (KnnmCluster cluster : clusterList) {
//                    System.out.println(cluster);
//                }

                IknnmCluster cluster = clusterList.get(0);
                System.out.println("cluster.num.size(): " + cluster.num.size());
                if (cluster.num.size() < 2) {
                    break;
                }
                iknnm.add(cluster);
                for (iknnInstance instance : cluster.num) {
                    instanceCovered.add(instance);
                }
            }
            // end of train

            for (IknnmCluster cluster : iknnm) {
                System.out.println(String.format("Cluster: <Cls=%s, Sim=%s, Num=%s, Rep=%s>",
                        cluster.cls.toString(),
                        cluster.sim,
                        cluster.num.size(),
                        cluster.rep.toString()));
            }

            Iknnm trainIknnm = new Iknnm();

            ArrayList<iknnInstance> trainInstances = new ArrayList<>();
            ArrayList<String> appNameList = new ArrayList<>();
            for (iknnInstance instance : trainDataSet) {
                trainInstances.add(instance);
                appNameList.add(instance.getApp());
            }

            iknnm = trainIknnm(iknnm, trainInstances, appNameList, 0, false);
            System.out.println("=========== Representatives After: ===========");
            System.out.println(iknnm);

            System.out.printf("第 %d 次交叉检验开始", fold+1);

            verify(testDataSet, iknnm,top);
        }
    }

    public static Iknnm trainIknnm(Iknnm representatives, ArrayList<iknnInstance> x, ArrayList<String> y, int erd,boolean isCorp){
        int status[];
        status = new int[x.size()];

        ArrayList<iknnInstance> correctlyClassifyInstances = new ArrayList<iknnInstance>();
        ArrayList<iknnInstance> inCorrectlyClassifyInstances = new ArrayList<iknnInstance>();
        for (int i = 0; i < x.size(); i++) {
            iknnInstance d = x.get(i);
            String actualClass = y.get(i);
            ArrayList<LabelIknnm> labelDistanceList = new ArrayList<>();
            for (IknnmCluster iknn1 : representatives) {
                double distance = computeDistance(d.getVector(), iknn1.rep.getVector());
                LabelIknnm labelIknn = new LabelIknnm();
                labelIknn.iknnmcluster = iknn1;
                labelIknn.distance = distance;
                labelDistanceList.add(labelIknn);
            }

            Collections.sort(labelDistanceList, new Comparator<LabelIknnm>() {
                @Override
                public int compare(LabelIknnm o1, LabelIknnm o2) {
                    return (int) (o1.distance - o2.distance);
                }
            });

            boolean isInCluster = false;
            boolean isClassified = false;
            for (LabelIknnm labeliknn1:labelDistanceList){
                IknnmCluster representative = labeliknn1.iknnmcluster;
                double distance = labeliknn1.distance;
                String predictedClass = representative.cls;
                double sim = representative.sim;

                if (distance <= sim){
                    isInCluster = true;  // In the cluster

                    if (actualClass.equals(predictedClass)) {
                        isClassified = true;
                        break;
                    }
                }
            }

            if (isInCluster){
                if (isClassified) {
                    status[i] = 1;
                    correctlyClassifyInstances.add(d);
                    for (LabelIknnm labelDistance: labelDistanceList) {
                        IknnmCluster representative = labelDistance.iknnmcluster;
                        double distance = labelDistance.distance;
                        String predictedClass = representative.cls;
                        double sim = representative.sim;

                        if (distance <= sim && actualClass.equals(predictedClass)){
                            representative.num.add(d);
                        }
                    }
                }else {
                    inCorrectlyClassifyInstances.add(d);
                }
            }else { // No covered by any cluster
                correctlyClassifyInstances.add(d);

                LabelIknnm labelDistance = labelDistanceList.get(0);
                IknnmCluster representative = labelDistance.iknnmcluster;
                String predictedClass = representative.cls;

                for (int m = 0; m < representatives.size(); m++) {
                    IknnmCluster rep1 = representatives.get(m);

                    if (rep1.cls.equals(predictedClass)) {
                        boolean canExtend = true;
                        for (int n = 0; n < representatives.size(); n++) {
                            if (n == m)
                                continue;
                            IknnmCluster rep2 = representatives.get(n);
                            double distance = computeDistance(rep1.rep.getVector(), rep2.rep.getVector());
                            if ((distance < rep1.sim + rep2.sim) && (rep1.cls != rep2.cls)){
                                canExtend = false;
                                break;
                            }
                        }
                        if (canExtend){
                            rep1.num.add(d);
                            double distance = computeDistance(rep1.rep.getVector(), d.getVector());
                            rep1.sim = distance;
                            representatives.remove(m);
                            representatives.add(m, rep1);
                            status[i] = 1;
                            break;
                        }
                    }
                }
            }
        }

        System.out.println("Correctly classified instances: " + correctlyClassifyInstances.size());
        System.out.println("Incorrectly classified instances: " + inCorrectlyClassifyInstances.size());

        // Done step 1 ~ 5

        ArrayList<ReqInstance> repInstList = new ArrayList<ReqInstance>();
        for (IknnmCluster rep : representatives) {
            ReqInstance reqInstance = new ReqInstance();
            reqInstance.rep = rep;
            reqInstance.instances = rep.num;
            repInstList.add(reqInstance);
        }

        for (iknnInstance e : inCorrectlyClassifyInstances) {
            for (ReqInstance repInst : repInstList) {
                IknnmCluster rep = repInst.rep;
                ArrayList<iknnInstance> inst =  repInst.instances;
                double sim = rep.sim;

                double d = computeDistance(e.getVector(), rep.rep.getVector());

                if (d <= sim)
                    inst.add(e);
            }
        }

        float minDensity = 0;
        System.out.println("repInstList => " +repInstList.toString());
        for (int i = 0; i < repInstList.size(); i++) {
            ReqInstance repInst = repInstList.get(i);
            System.out.println("repInst => "+ repInst.instances.toString());
            IknnmCluster rep = repInst.rep;
            ArrayList<iknnInstance> inst = repInst.instances;
            if (inst.size() == 0)
                continue;

            int num = inst.size();
            double sim = rep.sim;

            float w = 0;
            System.out.print("inst = " + inst.size());

            for (int j = 0; j < inst.size(); j++) {
                double d = computeDistance(inst.get(j).getVector(), rep.rep.getVector());
                double wj = 0;
                if (d > 0 && sim > 0) {
                    wj = d / sim;
                }
                w+= wj;
                System.out.print("d=" + d + ", sim=" + sim + ", wj="+ wj);
            }
            w /= sim;
            System.out.print("w="+ w);

            if (w < minDensity)
                minDensity = w;

            float f = w / num;
            float threshold = 1;

            System.out.print("w = "+w+", num = "+num+", f = "+f+", threshold = "+threshold+", min_density = "+minDensity);
            if (f <= threshold) {
                for (int j = 0; j < representatives.size(); j++) {
                    IknnmCluster r = representatives.get(j);
                    if (r == rep) {
                        IknnmCluster newLay = representatives.get(j);
                        newLay.lay+=1;
                        representatives.remove(j);
                        representatives.add(j, newLay);
                        break;
                    }
                }
            }
        }

        ArrayList<Integer> notCoverd = getNotCovered(status);
        System.out.println("size of not covered:"+ notCoverd.size());
        System.out.println("not covered:"+ notCoverd);

        ArrayList<iknnInstance>  new_x = new ArrayList<iknnInstance>();
        for (int i : notCoverd) {  // x = x[not_covered]
            new_x.add(x.get(i));
        }
        x = new_x;
        System.out.println("size of not covered:"+ notCoverd.size());

        ArrayList<String> labels = new ArrayList<String>();
        for (String label : y) {
            labels.add(label);
        }
        int[] newStatus = new int[x.size()];
        for (int i = 0; i < newStatus.length; i++) {
            newStatus[i]= 0;
        }
        status = newStatus;
        notCoverd = getNotCovered(status);
        ArrayList<ArrayList<Double>> distanceMatrix = getDistanceMatrix(x);

        int lay = 0;

        while (notCoverd.size() > 0) {
            ArrayList<Double> maxNeighbourhood = new ArrayList<>();
            int tuple_max_neighbourhood = 0;
            for (int i = 0; i < notCoverd.size(); i++) {
                ArrayList<Double> distances = distanceMatrix.get(i);

                // sort distance
                Collections.sort(distances);

                ArrayList<Double> sorted_distance = new ArrayList<>();
                sorted_distance = distances;

                // filter only those which has not been yet covered
                for (int j = 0; j < sorted_distance.size(); j++) {
                    if (status[j] == 0) {
                        sorted_distance.remove(j);
                    }
                }

                // compute neighbourhood
                int q = 0;
                ArrayList<Double> neighbourhood = new ArrayList<>();
                int error = 0;
                while(q < sorted_distance.size() && (labels.get(sorted_distance.get(q).intValue()).equals(labels.get(i)) || error<erd)) { // 这里处理错分率
                    neighbourhood.add(sorted_distance.get(q));
                    if (labels.get(sorted_distance.get(q).intValue()) != labels.get(i)){
                        error++; // 计算错分率
                    }
                    q+=1;
                }

                if (neighbourhood.size() > maxNeighbourhood.size()) {
                    maxNeighbourhood = neighbourhood;
                    tuple_max_neighbourhood = i;
                }
            }
            // add representative
            // representatives format (rep(di), all_tuples in neighbourhood, class(di), Sim(di))
            iknnInstance rep = x.get(tuple_max_neighbourhood);
            ArrayList<iknnInstance> num = new ArrayList<>();
            for (Double i : maxNeighbourhood) {
                num.add(x.get(i.intValue()));
            }
            String cls = labels.get(tuple_max_neighbourhood);
            Double sim = distanceMatrix.get(tuple_max_neighbourhood).get(0);
            IknnmCluster newReps = new IknnmCluster(rep, num, cls, sim, 0);

            for (int i = 0; i < maxNeighbourhood.size(); i++) {
                status[i] = 1;
            }
            notCoverd = getNotCovered(status);

            representatives.add(newReps);
            System.out.println("size of not covered:"+ notCoverd.size());
        }

        // sort lay
        Collections.sort(representatives, new Comparator<IknnmCluster>() {
            @Override
            public int compare(IknnmCluster o1, IknnmCluster o2) {
                return o1.lay - o2.lay;
            }
        });
        System.out.println(representatives.toString());
        return representatives;
    }

    private static ArrayList<Integer> getNotCovered(int[] status) {
        ArrayList<Integer> notCovered = new ArrayList<>();
        for (int i = 0; i < status.length; i++) {
            if (status[i] != 0) {
                notCovered.add(status[i]);
            }
        }
        return notCovered;
    }

    private static ArrayList<ArrayList<Double>> getDistanceMatrix(ArrayList<iknnInstance> instances){
        ArrayList<ArrayList<Double>> distanceMatrix = new ArrayList<>();
        for (int i = 0; i < instances.size(); i++) {
            ArrayList<Double> distance = new ArrayList<>();
            for (int j = 0; j < instances.size(); j++) {
                distance.add(computeDistance(instances.get(i).getVector(), instances.get(j).getVector()));
            }
            distanceMatrix.add(distance);
        }
        return distanceMatrix;
    }

    public static ArrayList<ArrayList<String>> classify_all(ArrayList<iknnInstance> instances , Iknnm iknnm, int topK, boolean isCrop) {
        // 原  <rep, num, cls, sim, lay, fac, cor> iknn model
        ArrayList<ArrayList<String>> predictedLabelList = new ArrayList<>();
        long startClassTime, endClassTime;
        int classifyTimes = 0;
        startClassTime = System.currentTimeMillis();
        for (int i = 1;i < topK+1;i++) {
            ArrayList<String> predictedLabels = new ArrayList<>();
            for (int j = 0;j< instances.size()-1;j++) {
                ArrayList<String> labels = classify(instances.get(j), iknnm, topK, isCrop);
                predictedLabels.addAll(labels);
                classifyTimes += 1;
            }
            predictedLabelList.add(predictedLabels);
        }
        endClassTime = System.currentTimeMillis();
        System.out.println("Classify "+ classifyTimes+" times. Cost "+ (endClassTime - startClassTime)
                + " ms, Average classify cost "+((endClassTime - startClassTime)/classifyTimes)+" ms.");
        return predictedLabelList;
    }

    private static ArrayList<String> classify(iknnInstance instance , Iknnm iknnm, int topK, boolean isCrop) {
        ArrayList<LabelIknnm> labelDistanceList = new ArrayList<>();
        Iknnm inReq  = new Iknnm();

        for (IknnmCluster iknn : iknnm) {
            double distance = computeDistance(instance.getVector(), iknn.rep.getVector());
            LabelIknnm labIknn = new LabelIknnm();
            labIknn.iknnmcluster = iknn;
            labIknn.distance = distance;
            labelDistanceList.add(labIknn);
            if (distance <= iknn.sim) {
                inReq.add(iknn);
            }

            Collections.sort(labelDistanceList, new Comparator<LabelIknnm>() {
                @Override
                public int compare(LabelIknnm o1, LabelIknnm o2) {
                    return (int) (o1.distance - o2.distance);
                }
            });
        }

        ArrayList<String> labels = new ArrayList<>();

        for (int k = 0; k < topK-1; k++) {
            if (inReq.size() > 0) {
                boolean isSameClass = true;
                for (int i = 0; i < inReq.size(); i++) {
                    for (int j = 0; j < inReq.size(); j++) {
                        if (i == j)
                            continue;
                        if (inReq.get(i).cls != inReq.get(j).cls){
                            isSameClass = false;
                            break;
                        }
                    }
                }
                if (isSameClass) {
                    if (isCrop) {
                        for (IknnmCluster iknn2: inReq) {
                            iknn2.lay+=1;
                        }
                    }
                    labels.add(inReq.get(0).cls);
                    inReq.remove(0);
                }else {
                    int maxLay = 0;
                    for (IknnmCluster iknnc : inReq) {
                        if (iknnc.lay > maxLay)
                            maxLay = iknnc.lay;
                    }

                    Iknnm maxLayInReq = new Iknnm();
                    System.out.print("max_lay "+ maxLay);
                    for (IknnmCluster iknn3 : inReq){
                        if (iknn3.lay == maxLay) {
                            maxLayInReq.add(iknn3);
                        }
                    }

                    String cls = maxLayInReq.get(0).cls;
                    int maxNum = 0;
                    IknnmCluster r = maxLayInReq.get(0);
                    for (IknnmCluster iknn4: maxLayInReq) {
                        if (iknn4.num.size() > maxNum) {
                            cls = iknn4.cls;
                            maxNum = iknn4.num.size();
                            r = iknn4;
                        }
                    }

                    Iknnm newInRep = new Iknnm();
                    for (IknnmCluster iknnc : inReq) {
                        if (iknnc == r) {
                            continue;
                        }else{
                            newInRep.add(iknnc);
                        }
                    }
                    inReq = newInRep;

                    labels.add(cls);
                }

            }else {
                System.out.println("size of labelDistanceList:"+ labelDistanceList.size());
                int maxLay = 0;
                for (LabelIknnm li : labelDistanceList) {
                    if (li.iknnmcluster.lay > maxLay) {
                        maxLay = li.iknnmcluster.lay;
                    }
                }
                Collections.sort(labelDistanceList, new Comparator<LabelIknnm>() {
                    @Override
                    public int compare(LabelIknnm o1, LabelIknnm o2) {
                        return o1.iknnmcluster.lay - o2.iknnmcluster.lay;
                    }
                });

                ArrayList<LabelIknnm> sameLay = new ArrayList<>();
                for (LabelIknnm liknn1 : labelDistanceList) {
                    if (liknn1.iknnmcluster.lay == maxLay) {
                        System.out.println("liknn1:"+ liknn1.toString());
                        sameLay.add(liknn1);
                    }
                }
                System.out.println("maxLay:"+ maxLay);
                System.out.println("sameLay:"+ sameLay.toString());

                ArrayList<LabelIknnm> newLabelDistanceList = new ArrayList<>();
                for (LabelIknnm labels1 : labelDistanceList) {
                    if (sameLay.get(0).iknnmcluster == labels1.iknnmcluster &&
                            sameLay.get(0).distance == labels1.distance){
                        continue;
                    }else {
                        newLabelDistanceList.add(labels1);
                    }
                }
                labelDistanceList = newLabelDistanceList;
                if (isCrop) {
                    sameLay.get(0).iknnmcluster.lay+=1;
                }
                String cls = sameLay.get(0).iknnmcluster.cls;

                labels.add(cls);
            }
        }
        return labels;
    }

    private static void verify(Dataset testData,Iknnm representatives, int top) { // 必须先对 IknnModel 的lay进行排序
        ArrayList<ArrayList<String>> predictedLabelList = classify_all(testData, representatives, top, true);
        int[] totalMatched = new int[top];
        int[] totalUnmatched  = new int[top];
        int allMatched = 0;
        int allUnmatched = 0;

        System.out.println("size of predictedLabelList:" + predictedLabelList.size());
        for (int i = 0; i < top-1; i++) {
            totalMatched[i] = totalUnmatched[i] = 0;

            System.out.printf("===== Predicting %d apps =====\n", i+1);
            ArrayList<String> predictedLabels = predictedLabelList.get(i);
            System.out.println("size of predictedLabels:" + predictedLabels.size());
            System.out.println("predictedLabels:" + predictedLabels.toString());


            System.out.println("size of testData:" + testData.size());
            System.out.println("testData:" + testData.toString());
            for (int j = 0; j < testData.size()-1; j++) {
                boolean isMatched = false;
                for (int k = 0; k < i+1; k++) {
                    if (testData.get(j).getApp().equals(predictedLabels.get(j))){
                        isMatched = true;
                        break;
                    }
                }
                if (isMatched) {
                    totalMatched[i]+=1;
                } else {
                    totalUnmatched[i]+=1;
                }
            }
            // 本次预测的结果
            System.out.println("label test = " + testData.toString());
            System.out.println("predicted_labels = " + predictedLabels.toString());
            double accuracy = totalMatched[i] / (totalMatched[i] + totalUnmatched[i]);
            System.out.printf("Accuracy %f, totalMatched %d, totalUnmatched %d", accuracy,totalMatched[i], totalUnmatched[i]);

            // 综合交叉校验的结果
            allMatched += totalMatched[i];
            allUnmatched += totalUnmatched[i];
        }
        double average_accuracy = allMatched / (allMatched+allUnmatched);
        System.out.printf("本次交叉检验预测结果： Accuracy :%f, allMatched :%d, allUnmatched :%d", average_accuracy, allMatched, allUnmatched);

    }

    // computeDistance 计算欧式距离
    public static double computeDistance(double[] vector1, double[] vector2) {
        double distance = 0;

        if (vector1.length == vector2.length) {
            for (int i = 0; i < vector1.length; i++) {
                double temp = Math.pow((vector1[i] - vector2[i]), 2);
                distance += temp;
            }
            distance = Math.sqrt(distance);
        }
        return distance;
    }
}
