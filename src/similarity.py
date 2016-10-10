from gensim import corpora, models, similarities

file_object = open("out/set.txt")

model = models.Word2Vec.load('out/raw.model')

my_list = list()

for line1 in open("out/set.txt"):
    line1 = line1.strip("\n")
    my_list.append(line1)

for line1 in my_list:
    for line2 in my_list:
        try:
            similarity = model.similarity(line1, line2)
            print("%s %s %s" % (line1, line2, similarity))
        except Exception,e:
            print("%s %s" % (line1, line2)) 
