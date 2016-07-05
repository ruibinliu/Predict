
package mo.edu.must.perdict;

import mo.edu.must.perdict.preprocess.PreProcessMain;
import mo.edu.must.perdict.tan.TanMain;

public class Main {
    public static void main(String[] args) {
        PreProcessMain.main(new String[] {
            "data/meizu2.txt"
        });

        TanMain.main(new String[] {
            "out/tan-data.txt"
        });
    }
}
