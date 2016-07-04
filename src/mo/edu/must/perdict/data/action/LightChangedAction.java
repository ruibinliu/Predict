package mo.edu.must.perdict.data.action;

import java.util.Date;

public class LightChangedAction extends Action {
    private String light;

    public LightChangedAction(Date time, String light) {
        eventId = EVENT_LIGHT_CHANGED;
        this.time = time;
        this.light = light;
    }

    @Override
    public String toString() {
        return "LightChangedAction[light=" + light + "]";
    }
}
