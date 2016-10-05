package mo.edu.must.perdict.lazy.knn2;

public class TimeEvent extends Event {

    public TimeEvent(String value) {
        super("Time", value);
    }

    @Override
    public float computeDistance(Event event) {
        if (!isSameType(event)) throw new IllegalArgumentException();

        return 0;
    }
}
