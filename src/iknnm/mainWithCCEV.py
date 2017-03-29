from knnModelWithCCEV import *


def main():
    # read data
    data = np.genfromtxt('../../out/vector_cevv_moto.txt', delimiter=" ", skip_header=False)

    x = data[:, 1:100]
    y = data[:, 0]
    z = data[:, 101:108] # save all last for count ccev

    classes, y = np.unique(y, return_inverse=True)

    kfold_cross_validation(x, y, z, 10)

if __name__ == "__main__":
    main()
