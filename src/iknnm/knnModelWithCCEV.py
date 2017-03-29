from scipy.spatial.distance import pdist, squareform, euclidean
from sklearn.cross_validation import KFold
import numpy as np
import time


def get_distance_matrix(x):
    return squareform(pdist(x, 'euclidean'))


def get_distance(a, b):
    return euclidean(a, b)


def get_not_covered(s):
    return [k for k in range(len(s)) if s[k] == 0]


def train_knnm(x, labels, lasts, erd, representatives=list()):
    m = len(x)
    states = np.zeros(m)  # Mark all the instance as 'not covered'
    distance_matrix = get_distance_matrix(x)
    not_covered = get_not_covered(states)

    # while there are examples to group
    while len(not_covered) > 0:
        max_neighbourhood = list()
        tuple_max_neighbourhood = None
        for i in not_covered:
            # get distance from i to all other tuples
            distances = distance_matrix[i]

            # sort distances
            sorted_distances = [d for d in sorted(enumerate(distances), key=lambda a: a[1])]

            # filter only those which has not been yet covered
            sorted_distances = [d for d in sorted_distances if states[d[0]] == 0]

            # compute neighbourhood
            q = 0
            neighbourhood = list()
            errors = 0
            while q < len(sorted_distances) and (labels[sorted_distances[q][0]] == labels[i] or errors < erd):
                neighbourhood.append(sorted_distances[q][0])
                if labels[sorted_distances[q][0]] != labels[i]:
                    errors += 1
                q += 1

            if len(neighbourhood) > len(max_neighbourhood):
                max_neighbourhood = neighbourhood
                tuple_max_neighbourhood = i
        # add representative
        # representatives format (rep(di), all_tuples in neighbourhood, class(di), Sim(di))
        rep = (tuple_max_neighbourhood, x[tuple_max_neighbourhood])
        num = max_neighbourhood
        cls = labels[tuple_max_neighbourhood]
        sim = distance_matrix[tuple_max_neighbourhood, max_neighbourhood[-1]]
        lay = 0
        representatives.append([rep, num, cls, sim, lay, 0, 0, lasts])

        # update states array
        for i in max_neighbourhood:
            states[i] = 1
        not_covered = get_not_covered(states)

        # print(len(representatives), len(representatives[len(representatives)-1][1]))

    return representatives


def same(rep1, rep2):
    return rep1[0][0] == rep2[0][0] and rep1[1] == rep2[1] and rep1[2] == rep2[2] and rep1[3] == rep2[3]


def train_iknnm(representatives, x, y, z, erd, is_crop=False):
    states = np.zeros(len(x))  # Mark all the instance as 'not covered'
    distance_matrix = get_distance_matrix(x)

    correctly_classify_instances = list()
    incorrectly_classify_instances = list()
    for i in range(0, len(x)):  # foreach single data
        d = x[i]
        actual_class = y[i]

        label_distance_list = list()
        for representative in representatives:
            distance = get_distance(d, representative[0][1])
            label_distance_list.append((representative, distance))

        # Sort the result by their distance against x
        label_distance_list.sort(key=lambda a: a[1])

        # Select the nearest k labels as the return value

        # Calculate the distance, define the incorrectly classified instances
        is_in_cluster = False
        is_classified = False
        for label_distance in label_distance_list:
            representative = label_distance[0]
            distance = label_distance[1]
            predicted_class = representative[2]
            sim = representative[3]

            if distance <= sim:
                # In the cluster
                is_in_cluster = True

                if actual_class == predicted_class:
                    is_classified = True
                    break

        if is_in_cluster:
            if is_classified:
                states[i] = 1
                correctly_classify_instances.append(d)
                for label_distance in label_distance_list:
                    representative = label_distance[0]
                    distance = label_distance[1]
                    predicted_class = representative[2]
                    sim = representative[3]

                    if distance <= sim and actual_class == predicted_class:
                        representative[1].append(i)  # FIXME The index 'i' is incorrect in the all_tuples.
            else:
                incorrectly_classify_instances.append(d)
        else:  # No covered by any cluster
            correctly_classify_instances.append(d)

            label_distance = label_distance_list[0]
            representative = label_distance[0]
            predicted_class = representative[2]

            for m in range(0, len(representatives)):
                rep1 = representatives[m]

                if rep1[2] == predicted_class:
                    # Extend the representative
                    can_extend = True
                    for n in range(0, len(representatives)):
                        if n == m:
                            continue
                        rep2 = representatives[n]
                        distance = get_distance(rep1[0][1], rep2[0][1])
                        if distance < rep1[3] + rep2[3] and rep1[2] != rep2[2]:
                            can_extend = False
                            break
                    if can_extend:
                        rep1[1].append(i)
                        distance = get_distance(rep1[0][1], d)
                        representatives[m] = [rep1[0], rep1[1], rep1[2], distance, rep1[4], 0, 0, rep1[7]]
                        states[i] = 1
                        break

    print("Correctly classified instances: %d" % len(correctly_classify_instances))
    print("Incorrectly classified instances: %d" % len(incorrectly_classify_instances))

    # Done step1 to step5

    rep_inst_list = list()
    for rep in representatives:
        rep_inst_list.append((rep, list()))
    for e in incorrectly_classify_instances:
        for rep_inst in rep_inst_list:
            rep = rep_inst[0]
            inst = rep_inst[1]
            sim = rep[3]
            d = get_distance(e, rep[0][1])

            if d <= sim:
                inst.append(e)

    min_density = 0
    for i in range(0, len(rep_inst_list)):
        rep_inst = rep_inst_list[i]
        rep = rep_inst[0]
        inst = rep_inst[1]
        if len(inst) == 0:
            continue

        num = len(rep[1])
        sim = rep[3]

        w = 0
        print("inst = %s" % len(inst))
        for j in range(0, len(inst)):
            d = get_distance(inst[j], rep[0][1])
            wj = 0
            if d > 0 and sim > 0:
                wj = d / sim
            w += wj
            print("d = %s, sim = %s, wj = %s" % (d, sim, wj))
        w = w / sim
        print("w = %s" % w)

        if w < min_density:
            min_density = w

        f = w / num
        threshold = 1
        print("w = %s, num = %s, f = %s, threshold = %s, min_density = %s" % (w, num, f, threshold, min_density))

        if f <= threshold:
            for j in range(0, len(representatives)):
                r = representatives[j]
                if same(r, rep):
                    representatives[j] = [rep[0], rep[1], rep[2], rep[3], rep[4] + 1]
                    break

    not_covered = get_not_covered(states)
    print("not_covered = %d" % len(not_covered))
    print("not_covered = %s" % not_covered)

    # while there are examples to group
    x = x[not_covered]
    labels = list()
    for label in y:
        labels.append(label)
    states = np.zeros(len(x))  # Mark all the instance as 'not covered'
    not_covered = get_not_covered(states)
    distance_matrix = get_distance_matrix(x)
    new_reps = list()

    lay = 0

    while len(not_covered) > 0:
        max_neighbourhood = list()
        tuple_max_neighbourhood = None
        for i in not_covered:
            # get distance from i to all other tuples
            distances = distance_matrix[i]

            # sort distances
            sorted_distances = [d for d in sorted(enumerate(distances), key=lambda a: a[1])]

            # filter only those which has not been yet covered
            sorted_distances = [d for d in sorted_distances if states[d[0]] == 0]

            # compute neighbourhood
            q = 0
            neighbourhood = list()
            errors = 0
            while q < len(sorted_distances) and (labels[sorted_distances[q][0]] == labels[i] or errors < erd):
                neighbourhood.append(sorted_distances[q][0])
                if labels[sorted_distances[q][0]] != labels[i]:
                    errors += 1
                q += 1

            if len(neighbourhood) > len(max_neighbourhood):
                max_neighbourhood = neighbourhood
                tuple_max_neighbourhood = i
        # add representative
        # representatives format (rep(di), all_tuples in neighbourhood, class(di), Sim(di))
        rep = (tuple_max_neighbourhood, x[tuple_max_neighbourhood])
        num = max_neighbourhood
        cls = labels[tuple_max_neighbourhood]
        sim = distance_matrix[tuple_max_neighbourhood, max_neighbourhood[-1]]
        new_rep = [rep, num, cls, sim, lay, 0, 0, [z[tuple_max_neighbourhood]]]
        if count_ccev(new_rep) == count_ccev(representatives[tuple_max_neighbourhood]):
            new_reps.append(new_rep)  # only same ccev can be added representative
        

        # update states array
        for i in max_neighbourhood:
            states[i] = 1
        not_covered = get_not_covered(states)

    for rep in new_reps:
        print("new_reps = <rep=%s, num=%s, cls=%s, sim=%s, lay=%s>"
              % (rep[0][0], len(rep[1]), rep[2], rep[3], rep[4]))
        representatives.append(rep)

    return representatives


def classify(x, representatives, top_k, is_crop=False):
    label_distance_list = list()
    in_reps = list()
    for rep in representatives:
        distance = get_distance(x, rep[0][1])
        sim = rep[3]
        label_distance_list.append((rep, distance))
        if distance <= sim:
            in_reps.append(rep)

    # Sort the result by their distance against x
    label_distance_list.sort(key=lambda a: a[1])

    labels = list()
    for k in range(0, top_k):
        if len(in_reps) > 0:
            is_same_class = True
            for i in range(0, len(in_reps)):
                for j in range(0, len(in_reps)):
                    if i == j:
                        continue
                    if in_reps[i][2] != in_reps[j][2]:
                        is_same_class = False
                        break
            if is_same_class:
                if is_crop:
                    for r in in_reps:
                        r[6] += 1 # Increase the correctly classified instance number
                cls = in_reps[0][2]
                in_reps.remove(in_reps[0])
                labels.append(cls)
            else:
                max_lay = max(in_reps, key=lambda a: a[4])[4]
                # print("max_lay = ", max_lay)
                max_lay_in_reps = list()
                for rep in in_reps:
                    if rep[4] == max_lay:
                        max_lay_in_reps.append(rep)

                cls = max_lay_in_reps[0][2]
                max_num = 0
                r = max_lay_in_reps[0]
                for rep in max_lay_in_reps:
                    if len(rep[1]) > max_num:
                        cls = rep[2]
                        max_num = rep[1]
                        r = rep
                # print("Covered. Selected cls: %d" % cls)
                # print("len(in_reps)=%d, len(r)=%d" % (len(in_reps[0]), len(r)))
                new_in_reps = list()
                for rep in in_reps:
                    if same(rep, r):
                        continue
                    else:
                        new_in_reps.append(rep)
                in_reps = new_in_reps

                labels.append(cls)
        else:
            # Not covered by any cluster
            max_lay = max(label_distance_list, key=lambda a: a[0][4])[0][4]
            label_distance_list.sort(key=lambda a: a[0][4])  # Sort by layer
            same_lay = list()
            for label_distance in label_distance_list:
                if label_distance[0][4] == max_lay:
                    same_lay.append(label_distance)

            new_label_distance_list = list()
            for label_distance_2 in label_distance_list:
                if same(same_lay[0][0], label_distance_2[0]) and same_lay[0][1] == label_distance_2[1]:
                    continue
                else:
                    new_label_distance_list.append(label_distance_2)
            label_distance_list = new_label_distance_list
            if is_crop:
                same_lay[0][0][6] += 1
            cls = same_lay[0][0][2]
            # print("Not covered. Selected cls: %d" % cls)
            labels.append(cls)

    # Select the nearest k labels as the return value

    # for i in range(0, top_k):
    #     label_distance = label_distance_list[i]
    #     representative = label_distance[0]
    #     cls = representative[2]
    #     labels.append(cls)

    # print("predicted labels = ", labels)

    return labels

#               x_train_iknnm, iknnm, top_k, is_crop
def classify_all(x, representatives, top_k, is_crop=False):
    if is_crop:
        for rep in representatives:
            if len(rep) <= 5: # should never in here after ccev
                rep.append(0) # Delete factor
                rep.append(0) # Correctly classified instance number

    predicted_labels_list = list()
    start = time.clock()
    classify_times = 0
    for k in range(1, top_k + 1):
        predicted_labels = list()
        for i in range(x.shape[0]):  # length of x's first latitude
            labels = classify(x[i], representatives, k, is_crop)
            predicted_labels.append(labels)
            classify_times += 1
        predicted_labels_list.append(predicted_labels)
    end = time.clock()
    print("Classify %d times. Cost %f seconds. Average classify cost %f seconds." %
          (classify_times, (end - start), ((end - start) / classify_times)))

    for rep in representatives:
        if len(rep) > 6:
            if rep[6] == 0:
                rep[5] += 1

    return predicted_labels_list


def print_model(model):
    print("Model size: %d" % len(model))
    for rep in model:
        if len(rep) > 5:
            print("    <rep=%s, num=%s, cls=%s, sim=%s, lay=%s, fac=%s, cor=%s>   " % (rep[0][0], len(rep[1]), rep[2], rep[3], rep[4], rep[5], rep[6]))
        else:
            print("    <rep=%s, num=%s, cls=%s, sim=%s, lay=%s>" % (rep[0][0], len(rep[1]), rep[2], rep[3], rep[4]))

# return ccev value for a representative
def count_ccev(representative):
    change0，change1，change2，change3，change4，change5，change6，change7 = 0, 0, 0, 0, 0, 0, 0, 0
    freq0, freq1, freq2, freq3, freq4, freq5, freq6, freq7 = 0, 0, 0, 0, 0, 0, 0, 0
    for index, rep in enumerate(representative):
        if rep[7][0] != "null": # for frequeency
            freq0+=1
        if rep[7][1] != "null":
            freq1+=1
        if rep[7][2] != "null":
            freq2+=1
        if rep[7][3] != "null":
            freq3+=1
        if rep[7][4] != "null":
            freq4+=1
        if rep[7][5] != "null":
            freq5+=1
        if rep[7][6] != "null":
            freq6+=1
        if rep[7][7] != "null":
            freq7+=1
        if 0 == index: # for change
            continue
        if rep[7][0] != representative[index-1][7][0]:
            change0+=1
        if rep[7][1] != representative[index-1][7][1]:
            change1+=1
        if rep[7][2] != representative[index-1][7][2]:
            change2+=1
        if rep[7][3] != representative[index-1][7][3]:
            change3+=1
        if rep[7][4] != representative[index-1][7][4]:
            change4+=1
        if rep[7][5] != representative[index-1][7][5]:
            change5+=1
        if rep[7][6] != representative[index-1][7][6]:
            change6+=1
        if rep[7][7] != representative[index-1][7][7]:
            change7+=1
    
    index = np.argmax([change0+freq0, change1+freq1,  change2+freq2,  change3+freq3,  change4+freq4,  change5+freq5,  change6+freq6,  change7+freq7])
    print("the ccev of this representative is %d" % index+1)
    return index+1



def kfold_cross_validation(x, labels, lasts, k):
    kf = KFold(len(x), n_folds=k)
    fold_size = len(x) / k

    total_matched_fold = list()
    total_unmatched_fold = list()
    for train_index, test_index in kf:
        train_index_knnm = list()
        train_index_iknnm_list = list()
        labels_train_iknnm_list = list()
        lasts_train_iknnm_list = list()

        for i in range(0, (k / 2) * fold_size):
            train_index_knnm.append(train_index[i])
        print("train_index_knnm = %s" % train_index_knnm)

        for fold in range(k / 2, (k - 1)):
            train_index_iknnm = list()
            labels_iknnm = list()
            lasts_iknnm = list()
            for j in range(fold * fold_size, (fold + 1) * fold_size):
                train_index_iknnm.append(train_index[j])
                labels_iknnm.append(labels[j])
                lasts_iknnm.append(lasts[j])
            train_index_iknnm_list.append(train_index_iknnm)
            labels_train_iknnm_list.append(labels_iknnm)
            lasts_train_iknnm_list.append(lasts_iknnm)  # save last done
            print("train_index_iknnm = %s" % train_index_iknnm)
        print("test_index = %s" % test_index)

        # The splited training instances.
        x_train_knnm = x[train_index_knnm]
        x_train_iknnm_list = list()

        for train_index_iknnm in train_index_iknnm_list:
            x_train_iknnm_list.append(x[train_index_iknnm])
        x_test = x[test_index]
        labels_train_knnm = labels[train_index_knnm]
        lasts_train_knnm = lasts[train_index_knnm]
        labels_test = labels[test_index]

        # x_train = x[train_index]
        # x_test = x[test_index]
        # labels_train = labels[train_index]
        # labels_test = labels[test_index]

        # Dispatch the training instances for KNNM and IKNNM.
        # Use half of the training instances to build the KNNM.
        # Build the kNN Model, which is formed by a list of cluster
        start = time.clock()
        knnm = train_knnm(x_train_knnm, labels_train_knnm, lasts_train_knnm, 1)
        end = time.clock()
        print("Knnm training cost %f seconds" % (end - start))

        # Use the rest of training instances to build the IKNNM.
        print("Representatives Before:")
        print_model(knnm)

        top_k = 5
        is_crop = True
        k_delete_factor = 5 

        iknnm = knnm
        for i in range(0, len(x_train_iknnm_list)): 
            start = time.clock()
            print("===== IKNNM training the %d fold =====" % (i + 1))
            x_train_iknnm = x_train_iknnm_list[i]
            labels_train_iknnm = labels_train_iknnm_list[i]
            lasts_train_iknnm = lasts_train_iknnm_list[i] # get the last
            if is_crop:
                classify_all(x_train_iknnm, iknnm, top_k, is_crop)
                print_model(iknnm)
                new_iknnm = list()
                for rep in iknnm:
                    if rep[5] <= k_delete_factor:
                        new_iknnm.append(rep)
                iknnm = new_iknnm

            iknnm = train_iknnm(iknnm, x_train_iknnm, labels_train_iknnm, lasts_train_iknnm, 1)
            end = time.clock()
            max_lay = max(iknnm, key=lambda a: a[4])[4]
            print("IKnnm training cost %f seconds. Max lay is %d." % (end - start, max_lay))

        print("Representatives After:")
        print_model(iknnm)

        predicted_labels_list = classify_all(x_test, iknnm, top_k, is_crop)
        total_matched = list()
        total_unmatched = list()

        if len(total_matched_fold) < top_k:
            for m in range(0, top_k):
                total_matched_fold.append(0)
                total_unmatched_fold.append(0)
        for m in range(0, top_k):
            total_matched.append(0)
            total_unmatched.append(0)

            print("")
            print("===== Predicting %d apps =====" % (m + 1))

            predicted_labels = predicted_labels_list[m]

            for i in range(0, len(predicted_labels)):
                is_matched = False
                for j in range(0, m + 1):
                    if labels_test[i] == predicted_labels[i][j]:
                        is_matched = True
                        break
                if is_matched:
                    total_matched[m] += 1
                else:
                    total_unmatched[m] += 1
            print("labels_test = %s" % labels_test)
            print("predicted_labels = %s" % predicted_labels)
            accuracy = float(total_matched[m]) / (total_matched[m] + total_unmatched[m])
            print("Accuracy %f, totalMatched %d, totalUnmatched %d" % (accuracy, total_matched[m], total_unmatched[m]))

            total_matched_fold[m] += total_matched[m]
            total_unmatched_fold[m] += total_unmatched[m]
            average_accuracy = float(total_matched_fold[m]) / (total_matched_fold[m] + total_unmatched_fold[m])
            print("Average Accuracy %f, total_matched_fold %d, total_unmatched_fold %d" % (average_accuracy, total_matched_fold[m], total_unmatched_fold[m]))
