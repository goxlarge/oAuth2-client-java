package goxlarge.auth.client.common;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseAction<T extends Action<T>> implements Action<T> {

    private static final String DIM__ACTION = "Action";

    private static final String VAL__NONE = "None";

    private final Map<String, String> details = new HashMap<>();

    protected BaseAction(String action) {
        details.put(DIM__ACTION, action);
    }

    public T withDetail(String dimension, String value) {
        dimension = isEmpty(dimension) ? VAL__NONE : dimension;
        value = isEmpty(value) ? VAL__NONE : value;
        if(dimension.equals(DIM__ACTION)) {
            throw new IllegalArgumentException("Supplied dimension is reserved: " + dimension);
        }
        details.put(dimension, value);
        return getThis();
    }

    public T withDetails(Map<String, String> details) {
        details.forEach(this::withDetail);
        return getThis();
    }

    @Override
    public Map<String, String> getDetails() {
        return details;
    }

    protected abstract T getThis();

    private static boolean isEmpty(String value) {
        return value == null || value.isEmpty() || value.matches("^ +$");
    }
}