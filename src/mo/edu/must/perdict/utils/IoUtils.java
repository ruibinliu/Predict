package mo.edu.must.perdict.utils;

import java.io.Closeable;
import java.io.IOException;

public class IoUtils {
    public static void close(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
            }
        }
    }
}
