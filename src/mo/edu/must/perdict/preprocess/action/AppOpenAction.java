package mo.edu.must.perdict.preprocess.action;

import java.util.ArrayList;
import java.util.Date;

public class AppOpenAction extends Action {
    private String packName;
    private static ArrayList<String> packageIdCreator = new ArrayList<>();
    static {
        // Let the id begin from 1.
        packageIdCreator.add("");
    }

    public AppOpenAction(Date time, String packName) {
        eventId = EVENT_APP_OPEN;
        this.time = time;
        this.packName = packName;
    }

    public String getPackName() {
        return packName;
    }

    public void setPackName(String packName) {
        this.packName = packName;
    }

    @Override
    public String toString() {
        return "AppOpenAction[packName=" + packName + "]";
    }
}
