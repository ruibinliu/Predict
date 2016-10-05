package mo.edu.must.perdict.lazy.knn2;

import java.util.ArrayList;

public class Instance {
    private ArrayList<Event> sessionFeature;
    private Event event;

    public Instance() {
        sessionFeature = new ArrayList<>();
    }

    public ArrayList<Event> getSessionFeature() {
        return sessionFeature;
    }

    public void setSessionFeature(ArrayList<Event> sessionFeature) {
        this.sessionFeature = sessionFeature;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public float computeDistance(Instance instance) {
        float distance = 0.0f;
        for (int i = 0, size = sessionFeature.size(); i < size; i++) {
            distance += sessionFeature.get(i).computeDistance(instance.getSessionFeature().get(i));
        }
        return distance;
    }

    @Override
    public String toString() {
        return "Instance[sessionFeature=" + sessionFeature + ",event=" + event + "]";
    }
}
