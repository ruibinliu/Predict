from knnModelBased import *


def main():
    # read data
    data = np.genfromtxt('../../data/knnm-data.txt',  delimiter=" ", skip_header=False)

    x = data[:, 0:400]
    y = data[:, 400]

    # shuffle
    p = np.random.permutation(len(x))
    x = x[p]
    y = y[p]

    classes, y = np.unique(y, return_inverse=True)

    kfoldCrossValidation(x, y, 5)

if __name__ == "__main__":
    main()
