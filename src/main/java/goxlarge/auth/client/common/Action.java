package goxlarge.auth.client.common;

import org.slf4j.MDC;

import java.time.Instant;
import java.util.Map;

public interface Action<T extends Action<T>> {

    Map<String, String> getDetails();

    T withDetail(String dimension, String value);

    T withDetails(Map<String, String> details);

    // ============= ACTION METADATA DETAILS

    default T timestamp() { return timestamp(Instant.now()); }
    default T timestamp(Instant ts) { return timestamp(ts.toString()); }
    default T timestamp(String ts) { return withDetail("Timestamp", ts); }
    default T actionType(String value) { return withDetail("ActionType", value); }
    default T businessActionType() { return actionType("BusinessAction"); }
    default T dependencyInteractionActionType() { return actionType("DependencyInteraction"); }
    default T invocationResultActionType() { return actionType("InvocationResult"); }

    default T result(String value) { return withDetail("Result", value); }
    default T successful() { return result("Success"); }
    default T failure() { return result("Failure"); }
    default T rejected() { return result("Rejected"); }
    default T unknown() { return result("Unknown"); }
    default T ignored() { return result("Ignored"); }
    default T skipped() { return result("Skipped"); }

    default T reason(String reason) { return withDetail("Reason", reason); }

    default T durationMillis(Long durationMillis) {
        return withDetail("Duration", "" + durationMillis);
    }
    default T durationSinceStartMillis(Long startEpochMillis) { return durationMillis(Instant.now().toEpochMilli()-startEpochMillis); }
    default T durationSinceStart(Instant start) {
        return durationSinceStartMillis(start.toEpochMilli());
    }

    // ============= APPLICATION DETAILS

    default T applicationName(String value) { return withDetail("Application", value); }
    default T internalComponent(String value) { return withDetail("InternalComponent", value); }

    // ============= INVOCATION DETAILS

    default T traceId(String value) { return withDetail("TraceId", value); }
    default T traceIdFromMDC(String mdcKey) { return traceId(MDC.get(mdcKey)); }
    default T traceIdFromMDC() { return traceIdFromMDC("trace_id"); }

    default T awsRequestId(String value) { return withDetail("AWSRequestId", value); }
    default T awsRequestIdFromMDC(String mdcKey) { return traceId(MDC.get(mdcKey)); }
    default T awsRequestIdFromMDC() { return traceIdFromMDC("aws_request_id"); }

    // ============= AUTHORIZATION DETAILS

    default T partition(String value) { return withDetail("Partition", value); }
    default T subject(String value) { return withDetail("Subject", value); }
    default T clientId(String value) { return withDetail("ClientId", value); }
    default T tokenClass(String value) { return withDetail("TokenClass", value); }

}
