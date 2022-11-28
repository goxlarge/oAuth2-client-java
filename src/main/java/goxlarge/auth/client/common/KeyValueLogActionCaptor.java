package goxlarge.auth.client.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class KeyValueLogActionCaptor implements ActionCaptor {

    private static final Logger systemLog = LoggerFactory.getLogger(KeyValueLogActionCaptor.class);

    private static final String KEY_AND_VALUE_REGEX = "^[^\\]\\[=,]+$";
    private static final Pattern KEY_AND_VALUE_PATTERN = Pattern.compile(KEY_AND_VALUE_REGEX);

    private static final String ACTION_DETAILS_MISSING_ERROR = "No action details were supplied; potentially due to poor formatting. Not publishing action";
    private static final String ACTION_DETAILS_REGEX_ERROR = "Invalid actions detail supplied with key=%s, value=%s; can't contain '[', ']', '=', ','";
    private static final String ACTION_DETAILS_ROLLUP_ERROR = "Failed during action publishing to stringify the action details supplied for logging purposes, NONFATAL";

    private final Consumer<String> logConsumer;

    public KeyValueLogActionCaptor() {
        this(LoggerFactory.getLogger("KeyValueLogActionCaptor")::info);
    }

    /** used for unit testing only) */
    public KeyValueLogActionCaptor(Consumer<String> logConsumer) {
        this.logConsumer = logConsumer;
    }

    public void capture(final Action action) {
        try {
            // validate details are parsable
            if(action.getDetails() == null) {
                logActionPublishingFailure(ACTION_DETAILS_MISSING_ERROR);
                return;
            }
            Map<String, String> actionDetails = new HashMap<>(action.getDetails());

            for(String key:actionDetails.keySet()) {
                if(actionDetails.get(key) == null || actionDetails.get(key).length() == 0) {
                    actionDetails.put(key, "None");
                }
            }

            boolean validDetails = true;
            for(Map.Entry<String, String> detail:actionDetails.entrySet()) {
                if(!KEY_AND_VALUE_PATTERN.matcher(detail.getKey()).matches() || !KEY_AND_VALUE_PATTERN.matcher(detail.getValue()).matches()) {
                    logActionPublishingFailure(String.format(ACTION_DETAILS_REGEX_ERROR, detail.getKey(), detail.getValue()));
                    validDetails = false;
                }
            }
            if(!validDetails) {
                return;
            }

            // log out the metric
            String detailsString = actionDetails.entrySet()
                    .stream()
                    .sorted(Comparator.comparing(Map.Entry::getKey))
                    .map(e -> e.getKey() + "=" + e.getValue())
                    .collect(Collectors.joining(","));
            logConsumer.accept("ActionLog [" + detailsString + "]");
        } catch (Exception e) {
            logActionPublishingFailure(ACTION_DETAILS_ROLLUP_ERROR + " - Error message: " + e.getMessage());
        }
    }

    /*
     * if metric logs production fails, log a CW LogMetrics compatible log for alerting us to failures, to avoid blocking actual consumer traffic
     */
    protected static void logActionPublishingFailure(String errorMsg) {
        systemLog.warn("[ActionPublishingFailure] attempting to publish an action with error: {}", errorMsg);
    }

}