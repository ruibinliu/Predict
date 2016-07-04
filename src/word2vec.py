from gensim import corpora, models, similarities
import traceback

# Load sentence from file.
file_object = open('out/words.txt')
try:
    sentence = file_object.read()
finally:
    file_object.close()
# Split the sentence into words
texts = [[word for word in sentence.split()]]
# Train the model
model = models.Word2Vec(texts, size=50, window=5, sg=2, min_count=5, workers=4, hs=1)
# Save the model to disk
model.save('out/raw.model')

#print model['LightChangedAction[light=lighter]']

for line in open("out/set.txt"):
    line = line.strip('\n')
    print line,
    try:
        print model[line]
    except Exception,e:
        print ""
"""
file_object = open('b.txt', 'w')
file_object.writelines(model['LightChangedAction[light=lighter]'])
file_object.close( )
"""
