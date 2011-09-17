package time;

import java.util.HashMap;
import java.util.Map;

public class TimedCache {

    final private ObjectLoader loader;
    final private Map<String, TimestampedValue> cachedValues = new HashMap<String, TimestampedValue>();
    final private Clock clock;
    final private ReloadPolicy policy;

    public TimedCache(ObjectLoader loader, Clock clock, ReloadPolicy policy) {
        this.loader = loader;
        this.clock = clock;
        this.policy = policy;
    }

    public Object lookup(String key) {
        TimestampedValue found = cachedValues.get(key);

        if (found == null || policy.shouldReload(found.timestamp, clock.getCurrentTime())) {
            TimestampedValue relaodedValue = new TimestampedValue(loader.load(key), clock.getCurrentTime());
            cachedValues.put(key, relaodedValue);
        }
        return cachedValues.get(key).value;
    }

    private static class TimestampedValue {
        final public Object value;
        final public Timestamp timestamp;

        public TimestampedValue(Object value, Timestamp timestamp) {
            this.value = value;
            this.timestamp = timestamp;
        }
    }
}
