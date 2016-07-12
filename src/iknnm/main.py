from knnModelBased import *


def main():
    # read data
    data = np.genfromtxt('../../data/knnm-data.txt',  delimiter=" ", skip_header=False)

    x = data[:, 0:400]
    y = data[:, 400]

    classes, y = np.unique(y, return_inverse=True)

    kfold_cross_validation(x, y, 10)

if __name__ == "__main__":
    main()
