package mo.edu.must.perdict.lazy.knn2;


public class Event {
    protected String type;
    protected String value;

    public Event(String type, String value) {
        this.type = type;
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public boolean isSameType(Event event) {
        return type.equals(event.getType());
    }

    public float computeDistance(Event event) {
        if (event.getType().equals("AppOpened")) {
            return value.equals(event.getValue()) ? 0.0f : 1.0f;
        } else if (event.getType().equals("Time")) {
            long time1 = Long.valueOf(value);
            long time2 = Long.valueOf(event.getValue());
            long period = (time2 > time1) ? time2 - time1 : time1 - time2;
            float distance = (float) (((float)(period / 1000 / 60) % 1440) / 1440); // Normalize to 24 hour.
            return distance;
        } else {
            return 0.0f;
        }
    }

    @Override
    public String toString() {
        return "Event[type=" + type + ",value=" + value + "]";
    }
}
