package net.uncontended.precipice.metrics.latency;

import net.uncontended.precipice.metrics.AbstractMetrics;
import net.uncontended.precipice.metrics.IntervalIterator;
import net.uncontended.precipice.metrics.Rolling;
import net.uncontended.precipice.metrics.tools.Allocator;
import net.uncontended.precipice.metrics.tools.CircularBuffer;
import net.uncontended.precipice.metrics.tools.RollingMetrics;
import net.uncontended.precipice.time.SystemTime;

public class RollingLatency<T extends Enum<T>> extends AbstractMetrics<T> implements WritableLatency<T>, Rolling<PartitionedLatency<T>> {

    private final RollingMetrics<PartitionedLatency<T>> rolling;
    private final NoOpLatency<T> noOpLatency;

    public RollingLatency(Class<T> clazz, int buckets, long nanosPerBucket) {
        this(Latency.atomicHDRHistogram(clazz), buckets, nanosPerBucket);
    }

    public RollingLatency(Allocator<PartitionedLatency<T>> allocator, int buckets, long nanosPerBucket) {
        this(new RollingMetrics<PartitionedLatency<T>>(allocator,
                new CircularBuffer<PartitionedLatency<T>>(buckets, nanosPerBucket, System.nanoTime()),
                SystemTime.getInstance()));
    }

    public RollingLatency(RollingMetrics<PartitionedLatency<T>> rolling) {
        super(rolling.current().getMetricClazz());
        this.rolling = rolling;
        this.noOpLatency = new NoOpLatency<>(getMetricClazz());
    }

    @Override
    public void write(T result, long number, long nanoLatency, long nanoTime) {
        rolling.current(nanoTime).record(result, number, nanoLatency);
    }

    @Override
    public PartitionedLatency<T> current() {
        return rolling.current();
    }

    @Override
    public PartitionedLatency<T> current(long nanoTime) {
        return rolling.current(nanoTime);
    }

    @Override
    public IntervalIterator<PartitionedLatency<T>> intervals() {
        return rolling.intervalsWithDefault(noOpLatency);
    }

    @Override
    public IntervalIterator<PartitionedLatency<T>> intervals(long nanoTime) {
        return rolling.intervalsWithDefault(nanoTime, noOpLatency);
    }
}
