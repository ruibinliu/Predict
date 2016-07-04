package mo.edu.must.perdict.preprocess;

import java.util.ArrayList;

public class Corpus {
    private ArrayList<Document> documentList = new ArrayList<>();

    @Override
    public String toString() {
        return "Corpus [documentList=" + documentList + "]";
    }

    public static class Document extends ArrayList<Double> {
        private static final long serialVersionUID = 1L;
    }
}
