import numpy as np
from knnModelBased import *

def main():
    #read data
    data = np.loadtxt('../Data/fieldgoal.dat', usecols = (0,1))

    data = np.hsplit(data,2)
    X = data[0]
    y = data[1]


    classes, y = np.unique(y, return_inverse=True)

    #train with all data
    # representatives = train(X,y,0)
    # predicted_labels = classifyAll(X,representatives)
    print("5 fold cross validation avg accuracy: {}".format(kfoldCrossValidation(X,y, 5)))

if __name__ == "__main__":
    main()