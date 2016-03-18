/*
 * Copyright 2015 Timothy Brooks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package net.uncontended.precipice.metrics;


import net.uncontended.precipice.Failable;
import org.HdrHistogram.*;

import java.util.concurrent.TimeUnit;

public class IntervalLatencyMetrics<T extends Enum<T> & Failable> implements LatencyMetrics<T> {

    private final LatencyBucket[] buckets;
    private final long highestTrackableValue;
    private final int numberOfSignificantValueDigits;

    public IntervalLatencyMetrics(Class<T> type) {
        this(type, TimeUnit.HOURS.toNanos(1), 2);
    }

    public IntervalLatencyMetrics(Class<T> type, long highestTrackableValue, int numberOfSignificantValueDigits) {
        this.highestTrackableValue = highestTrackableValue;
        this.numberOfSignificantValueDigits = numberOfSignificantValueDigits;
        buckets = new LatencyBucket[type.getEnumConstants().length];
        for (int i = 0; i < buckets.length; ++i) {
            buckets[i] = new LatencyBucket(highestTrackableValue, numberOfSignificantValueDigits);
        }
    }

    @Override
    public void recordLatency(T result, long number, long nanoLatency) {
        recordLatency(result, number, nanoLatency, System.nanoTime());
    }

    @Override
    public void recordLatency(T result, long count, long nanoLatency, long nanoTime) {
        LatencyBucket bucket = getLatencyBucket(result);
        bucket.record(nanoLatency, count);
    }

    public Histogram totalHistogram(T result) {
        return totalHistogram(result, true);
    }

    public Histogram totalHistogram(T result, boolean shouldAllocate) {
        LatencyBucket bucket = getLatencyBucket(result);
        Histogram histogram = bucket.histogram;
        if (shouldAllocate) {
            Histogram newlyAllocated = new Histogram(highestTrackableValue, numberOfSignificantValueDigits);
            newlyAllocated.add(histogram);
            histogram = newlyAllocated;
        }
        return histogram;
    }

    public synchronized Histogram intervalHistogram(T result) {
        return intervalHistogram(result, true);
    }

    public synchronized Histogram intervalHistogram(T result, boolean shouldAllocate) {
        LatencyBucket bucket = getLatencyBucket(result);
        Histogram histogram = bucket.getIntervalHistogram();
        if (shouldAllocate) {
            Histogram newlyAllocated = new Histogram(highestTrackableValue, numberOfSignificantValueDigits);
            newlyAllocated.add(histogram);
            histogram = newlyAllocated;
        }
        return histogram;
    }

    private LatencyBucket getLatencyBucket(T result) {
        return buckets[result.ordinal()];
    }

    private static class LatencyBucket {
        private final Histogram histogram;
        private final Recorder recorder;
        private Histogram previousInterval;

        private LatencyBucket(long highestTrackableValue, int numberOfSignificantValueDigits) {
            histogram = new AtomicHistogram(highestTrackableValue, numberOfSignificantValueDigits);
            histogram.setStartTimeStamp(System.currentTimeMillis());

            recorder = new Recorder(highestTrackableValue, numberOfSignificantValueDigits);
            previousInterval = recorder.getIntervalHistogram();
        }

        private void record(long nanoLatency, long count) {
            recorder.recordValueWithCount(Math.min(nanoLatency, histogram.getHighestTrackableValue()), count);
        }

        private Histogram getIntervalHistogram() {
            Histogram intervalHistogram = recorder.getIntervalHistogram(previousInterval);
            previousInterval = intervalHistogram;
            histogram.add(intervalHistogram);
            return intervalHistogram;
        }
    }
}
