from scipy.spatial.distance import pdist, squareform, euclidean
import numpy as np
from sklearn.cross_validation import KFold
import time


def getDistanceMatrix(x):
    return squareform(pdist(x, 'euclidean'))


def getDistance(a, b):
    return euclidean(a, b)


def getUngrouped(s):
    ind = [k for k in range(len(s)) if s[k] == 0]
    return ind


def train(X, y, erd):
    m = X.shape[0]
    states = np.zeros(m)
    distMatrix = getDistanceMatrix(X)
    ungrouped = getUngrouped(states)

    representatives = list()
    # while there are examples to group
    while (len(ungrouped) > 0):
        maxNeighbourhood = list()
        tupleMaxNeighbourhood = None
        for i in ungrouped:
            # get distance from i to all other tuples
            distances = distMatrix[i]
            # sort distances
            sorted_distances = [d for d in sorted(enumerate(distances), key=lambda x: x[1])]
            # filter only those which has not been yet grouped
            sorted_distances = [d for d in sorted_distances if states[d[0]] == 0]
            # compute neigbourhood
            q = 0
            neighbourhood = list()
            errors = 0
            while (q < len(sorted_distances) and (y[sorted_distances[q][0]] == y[i] or errors < erd)):
                neighbourhood.append(sorted_distances[q][0])
                if y[sorted_distances[q][0]] != y[i]:
                    errors += 1
                q += 1

            if (len(neighbourhood) > len(maxNeighbourhood)):
                maxNeighbourhood = neighbourhood
                tupleMaxNeighbourhood = i
        # add representative
        # representatives format (rep(di), all_tuples in neighbourhood, class(di), Sim(di))
        rep = (tupleMaxNeighbourhood, X[tupleMaxNeighbourhood])
        num = maxNeighbourhood
        cls = y[tupleMaxNeighbourhood]
        sim = distMatrix[tupleMaxNeighbourhood, maxNeighbourhood[-1]]
        representatives.append((rep, num, cls, sim))
        # update states array
        for i in maxNeighbourhood:
            states[i] = 1
        ungrouped = getUngrouped(states)
        # print(len(representatives), len(representatives[len(representatives) - 1][1]))
    return representatives


def classify2(x, representatives, topK):
    labelDistanceList = list()
    for representative in representatives:
        distance = getDistance(x, representative[0][1])
        labelDistanceList.append((representative, distance))

    # Sort the result by their distance against x
    labelDistanceList.sort(key=lambda x: x[1])

    # Select the nearest k labels as the return value
    labels = list()
    for i in range(0, topK):
        lableDistance = labelDistanceList[i]
        representative = lableDistance[0]
        labels.append(representative[2])
    # label = min(representatives, key = lambda k: getDistance(x, k[0][1]))[2]
    return labels


def classify(x, representatives):
    covered = [r for r in representatives if getDistance(r[0][1], x) < r[3]]
    if (len(covered) == 1):
        return covered[0][2]
    elif (len(covered) > 1):
        sorted_covered = [d for d in sorted(covered, key=lambda x: len(x[1]))]
        return sorted_covered[0][2]
    elif (len(covered) == 0):
        label = min(representatives, key=lambda k: getDistance(x, k[0][1]))[2]
        return label


def classifyAll(X, representatives, topK):
    predicted_labels_list = list()
    start = time.clock()
    classifyTimes = 0;
    for k in range(1, topK + 1):
        predicted_labels = list()
        for i in range(X.shape[0]):
            labels = classify2(X[i], representatives, k)
            predicted_labels.append(labels)
            classifyTimes += 1
        predicted_labels_list.append(predicted_labels)
    end = time.clock()
    print("Classify %d times. Cost %f seconds. Average classify cost %f seconds." % (
            classifyTimes, (end - start), ((end - start) / classifyTimes)))

    return predicted_labels_list


def kfold_cross_validation(x, labels, k):
    kf = KFold(len(x), n_folds=k)
    # The accuracy of each fold testing.
    all_metrics_list = list()

    total_matched_fold = list()
    total_unmatched_fold = list()
    total_training_cost = 0
    total_training_times = 0
    for train_index, test_index in kf:
        print("train_index: %s" % train_index)
        # x_train = x[train_index]
        # labels_train = labels[train_index]
        # x_test = x[test_index]
        # labels_test = labels[test_index]

        fold_size = len(x) / k
        train_index_knnm_list = list()
        labels_train_knnm_list = list()

        train_index_knnm = list()
        labels_knnm = list()
        for i in range(0, (k / 2) * fold_size):
            train_index_knnm.append(train_index[i])
            labels_knnm.append(train_index[i])
        train_index_knnm_list.append(train_index_knnm)
        labels_train_knnm_list.append(labels_knnm)
        print("train_index_knnm = %s" % train_index_knnm)

        for fold in range(k / 2, (k - 1)):
            train_index_knnm = list()
            labels_knnm = list()
            for j in range(0, (fold + 1) * fold_size):
                train_index_knnm.append(train_index[j])
                labels_knnm.append(labels[j])
            train_index_knnm_list.append(train_index_knnm)
            labels_train_knnm_list.append(labels_knnm)
            print("train_index_knnm = %s" % train_index_knnm)
        print("test_index = %s" % test_index)

        # The splited training instances.
        x_train_knnm_list = list()
        for train_index_knnm in train_index_knnm_list:
            x_train_knnm_list.append(x[train_index_knnm])
        x_test = x[test_index]
        labels_test = labels[test_index]

        total_start = time.clock()
        representatives = None
        for i in range(0, len(x_train_knnm_list)):
            x_train_knnm = x_train_knnm_list[i]
            labels_train_knnm = labels_train_knnm_list[i]
            print("===== KNNM training the %d fold =====" % (len(x_train_knnm) / fold_size))

            start = time.clock()
            representatives = train(x_train_knnm, labels_train_knnm, 1)
            end = time.clock()
            print("Knnm training cost %f seconds" % (end - start))
        total_end = time.clock()
        total_training_cost += (total_end - total_start)
        total_training_times += 1
        print("Knnm training totally cost %f seconds" % (total_end - total_start))

        # Construct the kNN Model, which is founded by a list of cluster
        # start = time.clock()
        # representatives = train(x_train, labels_train, 1)
        # end = time.clock()
        # print("Training cost %d seconds." % (end - start))

        top_k = 5
        predictedLabelsList = classifyAll(x_test, representatives, top_k)
        totalMatched = list()
        totalUnmatched = list()

        if len(total_matched_fold) < top_k:
            for m in range(0, top_k):
                total_matched_fold.append(0)
                total_unmatched_fold.append(0)
        for m in range(0, top_k):
            totalMatched.append(0)
            totalUnmatched.append(0)

            print("")
            print("===== Predicting ", (m + 1), " apps =====")
            all_metrics = list()
            predictedLabels = predictedLabelsList[m]

            for i in range(0, len(predictedLabels)):
                matched = False
                for j in range(0, m + 1):
                    if labels_test[i] == predictedLabels[i][j]:
                        matched = True
                if matched:
                    totalMatched[m] += 1
                else:
                    totalUnmatched[m] += 1
            accuracy = float(float(totalMatched[m]) / (totalMatched[m] + totalUnmatched[m]))
            print("Accuracy %f, totalMatched %d, totalUnmatched %d." % (accuracy, totalMatched[m], totalUnmatched[m]))

            total_matched_fold[m] += totalMatched[m]
            total_unmatched_fold[m] += totalUnmatched[m]
            average_accuracy = float(total_matched_fold[m]) / (total_matched_fold[m] + total_unmatched_fold[m])
            print("Average Accuracy %f, total_matched_fold %d, total_unmatched_fold %d" % (
                    average_accuracy, total_matched_fold[m], total_unmatched_fold[m]))
    print("Total training time is %f seconds. Total training %d times. Average train cost %f seconds" % (
            total_training_cost, total_training_times, total_training_cost / total_training_times))

    # Returns the average of the array elements
    means = list()
    for all_metrics in all_metrics_list:
        means.append(np.mean(all_metrics, axis=0))
    return means
