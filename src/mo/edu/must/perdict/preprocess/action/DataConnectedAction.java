package mo.edu.must.perdict.preprocess.action;

import java.util.Date;

public class DataConnectedAction extends Action {

    public DataConnectedAction(Date time) {
        eventId = EVENT_DATA_CONNECTED;
        this.time = time;
    }

    public String toString() {
        return "DataConnectedAction[]";
    }
}
