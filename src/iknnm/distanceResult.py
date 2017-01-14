from scipy.spatial.distance import pdist, squareform, euclidean

vector1 = [[1,2,3]]
vector2 = [[4,5,6]]
vector3 = [[1,2,3], [4,5,6]]

print vector1   # [[1 2 3]]

euclidean(vector1, vector2)  # 5.196152422706632

pdist(vector3, 'euclidean')  # array([ 5.19615242])

squareform(pdist(vector1, 'euclidean'))  # array([[ 0.        ,  5.19615242],
                                         #       [ 5.19615242,  0.        ]])