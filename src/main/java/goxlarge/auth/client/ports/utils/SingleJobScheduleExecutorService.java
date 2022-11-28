package goxlarge.auth.client.ports.utils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class SingleJobScheduleExecutorService<T> {
    private final AtomicReference<ScheduledFuture> scheduledRefreshRef;
    private final ScheduledExecutorService scheduler;

    public SingleJobScheduleExecutorService() {
        this.scheduledRefreshRef = new AtomicReference<>();
        this.scheduler =  Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "scheduler-thread-1");
            t.setDaemon(true);
            return t;
        });;
    }

    public ScheduledFuture schedule(Runnable runnable, long delay){
        cancelScheduledJob();
        ScheduledFuture scheduledFuture = scheduler.schedule(runnable, delay, TimeUnit.MILLISECONDS);
        scheduledRefreshRef.set(scheduledFuture);

        return scheduledFuture;
    }

    public void cancelScheduledJob() {
        ScheduledFuture existingSchedule = scheduledRefreshRef.getAndSet(null);
        if(existingSchedule != null && !existingSchedule.isDone()) {
            //log.info("Found a pre-existing scheduled refresh and not done, canceling it" + "; AuthorizationMechanism: " + authorizationMechanism.toString());
            existingSchedule.cancel(false);
        }
    }
}
