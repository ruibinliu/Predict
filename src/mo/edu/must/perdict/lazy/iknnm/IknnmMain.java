package mo.edu.must.perdict.lazy.iknnm;

import mo.edu.must.perdict.lazy.knn.Dataset;
import mo.edu.must.perdict.lazy.knn.Instance;
import mo.edu.must.perdict.lazy.knn.KnnMain;
import mo.edu.must.perdict.utils.FileUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Created by HackerZ on 2016/12/24.
 */
public class IknnmMain {
    public static void main() {
        final ArrayList<String> records = new ArrayList<>();
        FileUtils.read("data/issue-1819.txt", new FileUtils.Listener() {
            @Override
            public void onReadLine(String line) {
                records.add(line);
            }
        });
        // records.remove(0); // 第一行是字段的说明

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

            final HashMap<Instance, HashMap<Instance, Float>> instanceDistance = new HashMap<>();
            for (int i = 0; i < trainDataSet.size(); i++) {
                Instance inst1 = trainDataSet.get(i);

                HashMap<Instance, Float> map = instanceDistance.get(inst1);
                if (map == null) {
                    map = new HashMap<>();
                    instanceDistance.put(inst1, map);
                }
                for (Instance inst2 : trainDataSet) {
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
            endBuildTime = System.currentTimeMillis();
            System.out.println("Calculate eight last instance cost:" + (endBuildTime - startBuildTime) + " ms");

            Iknnm trainIknnm = new Iknnm();
            Iknnm iknnm = new Iknnm();

            iknnm = trainIknnm(trainIknnm, iknnm, iknnm, 0, false);
            System.out.println("===========");
            System.out.println(iknnm);
        }
    }

    public static Iknnm trainIknnm(Iknnm representatives, Iknnm x, Iknnm y, int erd,boolean isCorp){
        int status[];
        status = new int[x.size()];

        Iknnm correctlyClassifyInstances = new Iknnm();
        Iknnm inCorrectlyClassifyInstances = new Iknnm();
        for (int i = 0; i < x.size(); i++) {
            IknnmCluster d = x.get(i);
            IknnmCluster actualClass = y.get(i);

            ArrayList<LabelIknnm> labelDistanceList = new ArrayList<>();
            for (IknnmCluster iknn1 : representatives) {
                float distance = KnnMain.computeDistance(d.req, iknn1.req);
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
                float distance = labeliknn1.distance;
                String predictedClass = representative.cls;
                float sim = representative.sim;

                if (distance <= sim){
                    isInCluster = true;  // In the cluster

                    if (actualClass.cls == predictedClass) {
                        isClassified = true;
                        break;
                    }
                }
            }

            if (isInCluster){
                if (isClassified) {
                    status[i] = 1;
                    correctlyClassifyInstances.add(d);
                    for (LabelIknnm labelDistance1: labelDistanceList) {
                        IknnmCluster representative = labelDistance1.iknnmcluster;
                        float distance = labelDistance1.distance;
                        String predictedClass = representative.cls;
                        float sim = representative.sim;

                        if (distance <= sim && actualClass.cls == predictedClass){
                            representative.num.add(d.num.get(i));
                        }
                    }
                }else {
                    inCorrectlyClassifyInstances.add(d);
                }
            }else { // No covered by any cluster
                correctlyClassifyInstances.add(d);

                LabelIknnm labelIknn2 = labelDistanceList.get(0);
                IknnmCluster iknncluster = labelIknn2.iknnmcluster;
                String predictedClass = representatives.get(i).cls;

                for (int m = 0; m < representatives.size(); m++) {
                    IknnmCluster rep1 = representatives.get(m);

                    if (rep1.cls == predictedClass) {
                        boolean canExtend = true;
                        for (int n = 0; n < representatives.size(); n++) {
                            if (n == m)
                                continue;
                            IknnmCluster rep2 = representatives.get(n);
                            float distance = KnnMain.computeDistance(rep1.req, rep2.req);
                            if ((distance < rep1.sim + rep2.sim) && (rep1.cls != rep2.cls)){
                                canExtend = false;
                                break;
                            }
                        }
                        if (canExtend){
                            rep1.num.add(d.num.get(i));
                            float distance = KnnMain.computeDistance(rep1.req, d.req);
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

        Iknnm repInstList = new Iknnm();
        for (IknnmCluster rep:representatives) {
            repInstList.add(rep);
        }

        for (IknnmCluster e: inCorrectlyClassifyInstances) {
            for (IknnmCluster repInst : repInstList) {
                Instance rep = repInst.req;
                ArrayList<Instance> inst =  repInst.num;
                float sim = repInst.sim;

                float d = KnnMain.computeDistance(e.num.get(0), rep);

                if (d <= sim)
                    inst.addAll(e.num);
            }
        }

        float minDistance = 0;
        for (int i = 0; i < repInstList.size(); i++) {
            IknnmCluster repInst = repInstList.get(i);
            Instance rep = repInst.req;
            ArrayList<Instance> inst = repInst.num;
            if (inst.size() == 0)
                continue;

            int num = inst.size();
            float sim = repInst.sim;

            float w = 0;
            System.out.print("inst = " + inst.size());

            for (int j = 0; j < inst.size(); j++) {
                float d = KnnMain.computeDistance(inst.get(j), repInst.req);
                float wj = 0;
                if (d > 0 && sim < 0) {
                    wj = d / sim;
                }
                w+= wj;
                System.out.print("d=" + d + ", sim=" + sim + ", wj="+ wj);
            }
            w /= sim;
            System.out.print("w="+ w);

            if (w < minDistance)
                minDistance = w;

            float f = w / num;
            float threshold = 1;

            System.out.print("w = "+w+", num = "+num+", f = "+f+", threshold = "+threshold+", min_density = "+minDistance);
            if (f <= threshold) {
                for (int j = 0; j < representatives.size(); j++) {
                    IknnmCluster r = representatives.get(j);
                    if (r == repInst) {
                        representatives.remove(j);
                        representatives.add(j, r);
                        break;
                    }
                }
            }
        }

        ArrayList<Integer> notCoverd = getNotCovered(status);

        for (int i : notCoverd) {
            x.remove(i);
        }
        Iknnm labels = new Iknnm();
        for (IknnmCluster label : y) {
            labels.add(label);
        }
        for (int i = 0; i < status.length; i++) {
            status[i] = 0;
        }
        notCoverd = getNotCovered(status);
        ArrayList<Iknnm> distanceMatrix = getDistanceMatrix(x);
        Iknnm newReps = new Iknnm();

        int lay = 0;

        while (notCoverd.size() > 0) {
            Iknnm maxNeighbourhood = new Iknnm();
            int tuple_max_neighbourhood = 0;
            for (int i = 0; i < notCoverd.size(); i++) {
                 Iknnm distances = new Iknnm();
                distances = distanceMatrix.get(i);

                // sort distance
                Collections.sort(distances, new Comparator<IknnmCluster>() {
                    @Override
                    public int compare(IknnmCluster o1, IknnmCluster o2) {
                        return o1.num.size() - o2.num.size();
                    }
                });

                Iknnm sorted_distance = distances;

                // filter only those which has not been yet covered
                for (int j = 0; j < sorted_distance.size(); j++) {
                    if (status[j] == 0) {
                        sorted_distance.remove(j);
                    }
                }

                // compute neighbourhood
                int q = 0;
                Iknnm neighbourhood = new Iknnm();
                int error = 0;
                while(q < sorted_distance.size() && labels.get(sorted_distance.get(q).num.size()) == labels.get(i) || error<erd) {
                    neighbourhood.add(sorted_distance.get(q));
                    if (labels.get(sorted_distance.get(q).num.size()) != labels.get(i)){
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
            IknnmCluster rep = x.get(tuple_max_neighbourhood);
            Iknnm num = maxNeighbourhood;
            IknnmCluster cls = labels.get(tuple_max_neighbourhood);
            Iknnm sim = distanceMatrix.get(tuple_max_neighbourhood);
            newReps.add(rep);

            for (int i = 0; i < maxNeighbourhood.size(); i++) {
                status[i] = 1;
            }
            notCoverd = getNotCovered(status);

            for (IknnmCluster r : newReps) {
                representatives.add(r);
            }

        }

        // sort lay
        Collections.sort(representatives, new Comparator<IknnmCluster>() {
            @Override
            public int compare(IknnmCluster o1, IknnmCluster o2) {
                return o1.lay - o2.lay;
            }
        });
        return representatives;
    }

    public static ArrayList<Integer> getNotCovered(int[] status) {
        ArrayList<Integer> notCovered = new ArrayList<>();
        for (int i = 0; i < status.length; i++) {
            if (status[i] != 0) {
                notCovered.add(status[i]);
            }
        }
        return notCovered;
    }

    // TODO getDistanceMatrix
    public static ArrayList<Iknnm> getDistanceMatrix(Iknnm distanceMatrix){
        return new ArrayList<>();
    }

    public static ArrayList<Iknnm> classify_all(Iknnm trainIknnm , Iknnm iknnm, int topK, boolean isCrop) {
        // 原  <rep, num, cls, sim, lay, fac, cor> iknn model
        ArrayList predictedLabelList = new ArrayList<Iknnm>();
        long startClassTime, endClassTime;
        int classifyTimes = 0;
        startClassTime = System.currentTimeMillis();
        for (int i = 1;i < topK+1;i++) {
            Iknnm predictedLabels = new Iknnm();
            for (int j = 0;j< trainIknnm.size();j++) {
                Iknnm labels = classify(trainIknnm.get(j), iknnm, topK, isCrop);
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

    public static Iknnm classify(IknnmCluster trainIknnm , Iknnm iknnm, int topK, boolean isCrop) {
        ArrayList<LabelIknnm> labelDistanceList = new ArrayList<>();
        Iknnm inReq  = new Iknnm();

        for (IknnmCluster iknn : iknnm) {
            float distance = KnnMain.computeDistance(trainIknnm.req, iknn.req);
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

        Iknnm labels = new Iknnm();

        for (int k = 0; k < topK; k++) {
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
                            // TODO iknn.correct += 1
                        }
                    }
                    inReq.remove(inReq.get(0));
                    labels.add(inReq.get(0));
                }else {
                    IknnmCluster maxIknnm = Collections.max(inReq, new Comparator<IknnmCluster>() {
                        @Override
                        public int compare(IknnmCluster o1, IknnmCluster o2) {
                            return o1.lay - o2.lay;
                        }
                    });
                    Iknnm maxLayInReq = new Iknnm();
                    int maxLay = maxIknnm.lay;
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
                    for (IknnmCluster iknn5 : inReq) {
                        if (iknn5 == r) {
                            continue;
                        }else{
                            newInRep.add(iknn5);
                        }
                    }
                    inReq = newInRep;

                    IknnmCluster IknnCls = new IknnmCluster(new Instance(""), new ArrayList<Instance>(), cls, 0,0);
                    labels.add(IknnCls);
                }

            }else {
                IknnmCluster maxIknnm = Collections.max(inReq, new Comparator<IknnmCluster>() {
                    @Override
                    public int compare(IknnmCluster o1, IknnmCluster o2) {
                        return o1.lay - o2.lay;
                    }
                });
                Iknnm maxLayInReq = new Iknnm();
                int maxLay = maxIknnm.lay;
                Collections.sort(labelDistanceList, new Comparator<LabelIknnm>() {
                    @Override
                    public int compare(LabelIknnm o1, LabelIknnm o2) {
                        return o1.iknnmcluster.lay - o2.iknnmcluster.lay;
                    }
                });

                ArrayList<LabelIknnm> sameLay = new ArrayList<>();
                for (LabelIknnm liknn1 : labelDistanceList) {
                    if (liknn1.iknnmcluster.lay == maxLay) {
                        sameLay.add(liknn1);
                    }
                }

                ArrayList<LabelIknnm> newLabelDistanceList = new ArrayList<>();
                for (LabelIknnm labels1 : labelDistanceList) {
                    if (sameLay.get(0).iknnmcluster.req == labels1.iknnmcluster.req &&
                            sameLay.get(0).distance == labels1.distance){
                        continue;
                    }else {
                        newLabelDistanceList.add(labels1);
                    }
                }
                labelDistanceList = newLabelDistanceList;
                String cls = sameLay.get(0).iknnmcluster.cls;

                IknnmCluster IknnCls = new IknnmCluster(new Instance(""), new ArrayList<Instance>(), cls, 0,0);
                labels.add(IknnCls);
            }
        }
        return labels;
    }

    public static void verify(Iknnm representatives, int top,Dataset testData) { // 必须先对 IknnModel 的lay进行排序
        int[] matched = new int[top];
        int[] unmatched  = new int[top];

        for (int i = 0; i < top; i++) {
            matched[i] = unmatched[i] = 0;
        }

        // 将数据放置在模型里，然后根据需要获取的预测个数，拿到最近的预测结果
        // 将预测结果放到 HashMap 里面匹配，如果有其中之一匹配成功，那就 match++ ，否则将 unmatch++
        // match/count : 准度
        for (int i = 0; i < testData.size(); i++) {
            for (int j = 0; j < top; j++) {
                // TODO 将数据放置在模型
                for (int k = 0; k < representatives.size(); k++) {
                    if (representatives.get(k).cls.equals(testData.get(i).getApp())) {
                        matched[i]++;
                    }else {
                        unmatched[i]++;
                    }
                }
            }
        }
        int matchCount = 0;
        for (int i = 0; i < matched.length; i++) {
            if (matched[i] > 0) {
                matchCount++;
            }
        }
        System.out.printf("总体预测准度为：%d\n", matchCount/testData.size());
    }
}
