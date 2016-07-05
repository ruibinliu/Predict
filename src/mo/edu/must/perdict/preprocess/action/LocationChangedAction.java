
package mo.edu.must.perdict.preprocess.action;

import java.util.Date;

public class LocationChangedAction extends Action {
    private String location;

    public LocationChangedAction(Date time, String location) {
        eventId = EVENT_LOCATION_CHANGED;
        this.time = time;
        this.location = location;
    }

    @Override
    public String toString() {
        return "LocationChangedAction[" + location + "]";
    }
}
