package mo.edu.must.perdict.lazy.knn2;

import java.util.ArrayList;

public class Dataset extends ArrayList<Instance> {
    private ArrayList<String> eventTypes;

    public Dataset() {
        super();
    }

    public Dataset(ArrayList<String> records) {
        eventTypes = new ArrayList<>();
        String types = records.remove(0);
        types = types.substring(0, types.lastIndexOf(",")); // 最后一个是AppOpened

        for (String type : types.split(",")) {
            eventTypes.add(type);
        }

        for (String record : records) {
            Instance instance = new Instance();
            String[] events = record.split(",");
            ArrayList<Event> sessionFeature  = new ArrayList<>();
            instance.setSessionFeature(sessionFeature);

            for (int i = 0; i < events.length; i++) {
                if (i + 1 == events.length) {
                    AppOpenedEvent event = new AppOpenedEvent(events[0]);
                    instance.setEvent(event);
                } else {
                    Event event = new Event(eventTypes.get(i), events[i]);
                    sessionFeature.add(event);
                }
            }

            add(instance);
        }
    }

    public ArrayList<String> getEventTypes() {
        return eventTypes;
    }
}
