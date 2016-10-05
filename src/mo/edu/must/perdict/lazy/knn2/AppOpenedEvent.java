package mo.edu.must.perdict.lazy.knn2;

public class AppOpenedEvent extends Event {

    public AppOpenedEvent(String value) {
        super("AppOpened", value);
    }

    @Override
    public float computeDistance(Event event) {
        if (!isSameType(event)) throw new IllegalArgumentException();

        return value.equals(event.getValue()) ? 0.0f : 1.0f;
    }
}
