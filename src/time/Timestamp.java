package time;

public class Timestamp {

    // FIXME
    final private String timestamp;

    public Timestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return timestamp;
    }

}
