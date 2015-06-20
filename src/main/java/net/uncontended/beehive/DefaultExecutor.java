package net.uncontended.beehive;

import net.uncontended.beehive.circuit.BreakerConfigBuilder;
import net.uncontended.beehive.circuit.CircuitBreaker;
import net.uncontended.beehive.circuit.DefaultCircuitBreaker;
import net.uncontended.beehive.concurrent.*;
import net.uncontended.beehive.metrics.ActionMetrics;
import net.uncontended.beehive.metrics.DefaultActionMetrics;
import net.uncontended.beehive.metrics.Metric;
import net.uncontended.beehive.timeout.ActionTimeout;
import net.uncontended.beehive.timeout.TimeoutService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by timbrooks on 12/23/14.
 */
public class DefaultExecutor extends AbstractServiceExecutor {

    private final AtomicBoolean isShutdown = new AtomicBoolean(false);
    private final ExecutorService service;
    private final TimeoutService timeoutService = TimeoutService.defaultTimeoutService;
    private final ExecutorSemaphore semaphore;


    public DefaultExecutor(ExecutorService service, int concurrencyLevel) {
        this(service, concurrencyLevel, new DefaultActionMetrics());
    }

    public DefaultExecutor(ExecutorService service, int concurrencyLevel, ActionMetrics actionMetrics) {
        this(service, concurrencyLevel, actionMetrics, new DefaultCircuitBreaker(actionMetrics, new
                BreakerConfigBuilder().failureThreshold(20).trailingPeriodMillis(5000).build()));
    }

    public DefaultExecutor(ExecutorService service, int concurrencyLevel, CircuitBreaker breaker) {
        this(service, concurrencyLevel, new DefaultActionMetrics(), breaker);
    }

    public DefaultExecutor(ExecutorService service, int concurrencyLevel, ActionMetrics actionMetrics, CircuitBreaker
            circuitBreaker) {
        super(circuitBreaker, actionMetrics);
        this.semaphore = new ExecutorSemaphore(concurrencyLevel);
        this.service = service;
    }

    @Override
    public <T> ResilientFuture<T> submitAction(ResilientAction<T> action, long millisTimeout) {
        return submitAction(action, (ResilientPromise<T>) null, millisTimeout);
    }

    @Override
    public <T> ResilientFuture<T> submitAction(ResilientAction<T> action, ResilientPromise<T> promise, long
            millisTimeout) {
        return submitAction(action, promise, null, millisTimeout);
    }

    @Override
    public <T> ResilientFuture<T> submitAction(ResilientAction<T> action, ResilientCallback<T> callback, long
            millisTimeout) {
        return submitAction(action, null, callback, millisTimeout);
    }

    @Override
    public <T> ResilientFuture<T> submitAction(final ResilientAction<T> action, final ResilientPromise<T> promise,
                                               final ResilientCallback<T> callback, long millisTimeout) {
        acquirePermitOrRejectIfActionNotAllowed();
        final AbstractResilientPromise<T> internalPromise = new DefaultResilientPromise<>();
        if (promise != null) {
            internalPromise.wrapPromise(promise);
        }
        try {
            RunnableFuture<Void> task = new ResilientTask<>(actionMetrics, semaphore, circuitBreaker, action, callback,
                    internalPromise, promise);
            service.execute(task);

            if (millisTimeout > MAX_TIMEOUT_MILLIS) {
                timeoutService.scheduleTimeout(new ActionTimeout(MAX_TIMEOUT_MILLIS, internalPromise, task));
            } else {
                timeoutService.scheduleTimeout(new ActionTimeout(millisTimeout, internalPromise, task));
            }
        } catch (RejectedExecutionException e) {
            actionMetrics.incrementMetricCount(Metric.QUEUE_FULL);
            semaphore.releasePermit();
            throw new RejectedActionException(RejectionReason.QUEUE_FULL);
        }

        if (promise != null) {
            return new ResilientFuture<>(promise);
        } else {
            return new ResilientFuture<>(internalPromise);
        }

    }

    @Override
    public <T> ResilientPromise<T> performAction(final ResilientAction<T> action) {
        ResilientPromise<T> promise = new SingleWriterResilientPromise<>();
        acquirePermitOrRejectIfActionNotAllowed();
        try {
            T result = action.run();
            promise.deliverResult(result);
        } catch (ActionTimeoutException e) {
            promise.setTimedOut();
        } catch (Exception e) {
            promise.deliverError(e);
        }

        actionMetrics.incrementMetricCount(Metric.statusToMetric(promise.getStatus()));
        semaphore.releasePermit();

        return promise;
    }

    @Override
    public void shutdown() {
        isShutdown.compareAndSet(false, true);
        service.shutdown();
    }

    private void acquirePermitOrRejectIfActionNotAllowed() {
        if (isShutdown.get()) {
            throw new RejectedActionException(RejectionReason.SERVICE_SHUTDOWN);
        }

        boolean isPermitAcquired = semaphore.acquirePermit();
        if (!isPermitAcquired) {
            actionMetrics.incrementMetricCount(Metric.MAX_CONCURRENCY_LEVEL_EXCEEDED);
            throw new RejectedActionException(RejectionReason.MAX_CONCURRENCY_LEVEL_EXCEEDED);
        }

        if (!circuitBreaker.allowAction()) {
            actionMetrics.incrementMetricCount(Metric.CIRCUIT_OPEN);
            semaphore.releasePermit();
            throw new RejectedActionException(RejectionReason.CIRCUIT_OPEN);
        }
    }

}
