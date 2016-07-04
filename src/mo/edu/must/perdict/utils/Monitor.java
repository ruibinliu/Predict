
package mo.edu.must.perdict.utils;

public class Monitor {
    /**
     * Run the r and return the time cost in milliseconds.
     * 
     * @param r Runnable object to be execute.
     * @return Time cost in milliseconds.
     */
    public static long run(Runnable r) {
        long t0, t1;
        t0 = System.currentTimeMillis();
        r.run();
        t1 = System.currentTimeMillis();
        return t1 - t0;
    }
}
