package time;

import java.util.HashMap;
import java.util.Map;

public class TimedCache {

    final private ObjectLoader loader;
    final private Map<String, Object> cachedValues = new HashMap<String, Object>();

    public TimedCache(ObjectLoader loader) {
        this.loader = loader;
    }

    public Object lookup(String key) {
        if (!cachedValues.containsKey(key)) {
            cachedValues.put(key, loader.load(key));
        }
        return cachedValues.get(key);
    }
}
