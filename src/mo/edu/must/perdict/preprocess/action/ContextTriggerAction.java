package mo.edu.must.perdict.preprocess.action;

import java.util.ArrayList;
import java.util.Date;

public class ContextTriggerAction extends Action {
    private String context;
    private static ArrayList<String> contextIdCreator = new ArrayList<>();
    static {
        // Let the id begin from 1.
        contextIdCreator.add("");
    }

    public ContextTriggerAction(Date time, String context) {
        eventId = EVENT_CONTEXT_TRIGGER;
        this.time = time;
        this.context = context;
    }

    @Override
    public String toString() {
        return "ContextTriggerAction[" + context + "]";
    }
}
