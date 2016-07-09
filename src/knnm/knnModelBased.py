from scipy.spatial.distance import pdist, squareform, euclidean
from scipy.spatial import distance
import numpy as np
from sklearn.cross_validation import KFold
from sklearn import metrics
import operator

def getDistanceMatrix(x):
    return squareform(pdist(x, 'euclidean'))

def getDistance(a,b):
    return euclidean(a,b)

def getUngrouped(s):
    ind =  [ k for k in range(len(s)) if s[k] == 0 ]
    return ind

def train(X,y, erd):
    m = X.shape[0]
    states = np.zeros(m)
    distMatrix = getDistanceMatrix(X)
    ungrouped = getUngrouped(states)

    representatives = list()
    #while there are examples to group
    while(len(ungrouped) > 0):
        maxNeighbourhood = list()
        tupleMaxNeighbourhood = None
        for i in ungrouped:
            #get distance from i to all other tuples
            distances = distMatrix[i]
            #sort distances
            sorted_distances = [d for d in sorted(enumerate(distances), key=lambda x:x[1])]
            #filter only those which has not been yet grouped
            sorted_distances = [d for d in sorted_distances if states[d[0]] == 0]
            #compute neigbourhood
            q = 0
            neighbourhood = list()
            errors = 0
            while (q < len(sorted_distances)and (y[sorted_distances[q][0]] == y[i] or errors < erd)):
                neighbourhood.append(sorted_distances[q][0])
                if(y[sorted_distances[q][0]] != y[i]):
                    errors += 1
                q+=1

            if (len(neighbourhood) > len(maxNeighbourhood)):
                maxNeighbourhood = neighbourhood
                tupleMaxNeighbourhood = i
        #add representative
        #representatives format (rep(di), all_tuples in neighbourhood, class(di), Sim(di))
        rep = (tupleMaxNeighbourhood,X[tupleMaxNeighbourhood])
        num = maxNeighbourhood
        cls = y[tupleMaxNeighbourhood]
        sim = distMatrix[tupleMaxNeighbourhood,maxNeighbourhood[-1]]
        representatives.append((rep, num, cls, sim))
        #update states array
        for i in maxNeighbourhood:
            states[i] = 1
        ungrouped = getUngrouped(states)
        #print(len(representatives), len(representatives[len(representatives)-1][1]))
    return representatives

def classify2(x, representatives, topK):
    labelDistanceList = list()
    for representative in representatives:
        distance = getDistance(x, representative[0][1])
        labelDistanceList.append((representative, distance))

    # Sort the result by their distance against x
    labelDistanceList.sort(key=lambda x:x[1])

    # Select the nearest k labels as the return value
    labels = list()
    for i in range(0, topK):
        lableDistance = labelDistanceList[i]
        representative = lableDistance[0]
        labels.append(representative[2])
    #label = min(representatives, key = lambda k: getDistance(x, k[0][1]))[2]
    return labels

def classify(x, representatives):
    covered = [r for r in representatives if getDistance(r[0][1],x) < r[3]]
    if(len(covered) == 1):
        return covered[0][2]
    elif (len(covered) > 1):
        sorted_covered = [d for d in sorted(covered, key=lambda x:len(x[1]))]
        return sorted_covered[0][2]
    elif (len(covered) == 0):
        label = min(representatives, key = lambda k: getDistance(x, k[0][1]))[2]
        return label

def classifyAll(X,representatives,topK):
    predicted_labels_list = list()
    for k in range(1, topK + 1):
        predicted_labels = list()
        for i in range(X.shape[0]):
            labels = classify2(X[i], representatives, k)
            predicted_labels.append(labels)
        predicted_labels_list.append(predicted_labels)
    return predicted_labels_list

def kfoldCrossValidation(X,labels, k):
    kf = KFold(len(X), n_folds=k)
    # The accuracy of each fold testing.
    all_metrics_list = list()
    for train_index, test_index in kf:
        X_train = X[train_index]
        labels_train = labels[train_index]
        X_test = X[test_index]
        labels_test = labels[test_index]

        # Construct the kNN Model, which is founded by a list of cluster
        representatives = train(X_train, labels_train, 1)

        topK = 5;
        predictedLabelsList = classifyAll(X_test,representatives,topK)
        totalMatched = list()
        totalUnmatched = list()

        for k in range(0, topK):
            totalMatched.append(0)
            totalUnmatched.append(0)

            print("")
            print("===== Predicting ", (k + 1), " apps =====")
            all_metrics = list()
            predictedLabels = predictedLabelsList[k]

            for i in range(0, len(predictedLabels)):
                matched = False
                for j in range(0, k + 1):
                    if (labels_test[i] == predictedLabels[i][j]): 
                        matched = True
                    #print(predictedLabels[i][j])
                if (matched):
                    totalMatched[k] = totalMatched[k]+1
                else:
                    totalUnmatched[k] = totalUnmatched[k]+1
            accuracy = float(float(totalMatched[k]) / (totalMatched[k] + totalUnmatched[k]))
            print("accuracy", accuracy, "totalMatched", totalMatched[k], "totalUnmatched", totalUnmatched[k])

    # Returns the average of the array elements
    means = list()
    for all_metrics in all_metrics_list:
        means.append(np.mean(all_metrics,axis=0))
    return means
