from knnModelBased import *
import operator


def main():
    # read data
    data = np.genfromtxt('../../out/vector3forMFU.txt',  delimiter=" ", skip_header=False)

    x = data[:, 1:100]
    y = data[:, 0]
    # print y
    # classes, y = np.unique(y, return_inverse=True)

    start = time.clock()
    mfu_map = {}
    for t in y:
        if mfu_map.has_key(t):
            mfu_map[t] = mfu_map[t] + 1
        else:
            mfu_map[t] = 0
    end = time.clock()
    print("MFU training cost %f seconds." % (end - start))

    start = time.clock()
    dict= sorted(mfu_map.iteritems(), key=lambda d:d[1], reverse = True)
    app1, ignored =  dict[0]
    app2, ignored =  dict[1]
    app3, ignored =  dict[2]
    app4, ignored =  dict[3]
    app5, ignored =  dict[4]
    
    end = time.clock()
    print("result of MFU: %s, %s, %s, %s, %s" % (app1, app2, app3, app4, app5))
    print("MFU verify cost %f seconds." % (end - start))



if __name__ == "__main__":
    main()
