import numpy as np
from knnModelBased import *
from sklearn import metrics

def main():
    #read data
    data = np.genfromtxt('../../data/knnm-data.txt',  delimiter=" ", skip_header=False)
    # data = np.genfromtxt('../Data/iris.data',  delimiter=",", skip_header=False)

    X = data[:,0:400]
    y = data[:,400]

    #shuffle
    p = np.random.permutation(len(X))
    X = X[p]
    y = y[p]

    classes, y = np.unique(y, return_inverse=True)

    #train with all data
    representatives = train(X,y,0)
    #predicted_labels = classifyAll(X,representatives, 5)
    accuracyList = kfoldCrossValidation(X,y,5)
    for i in range(0, len(accuracyList)):
        print("===== Predicting ", i, " apps =====")
        print("5 fold cross validation avg accuracy: {}".format(kfoldCrossValidation(X,y,5)))

#     graph number of representatives for different values of erd

if __name__ == "__main__":
    main()
