package mo.edu.must.perdict.data.action;

import java.util.Date;

public class ChargeCableAction extends Action {
    public ChargeCableAction(Date time) {
        eventId = EVENT_CHARGE_CABLE;
        this.time = time;
    }

    @Override
    public String toString() {
        return "ChargeCableAction[]";
    }
}
