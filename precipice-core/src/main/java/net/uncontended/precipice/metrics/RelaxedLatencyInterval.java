/*
 * Copyright 2016 Timothy Brooks
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

import net.uncontended.precipice.time.Clock;
import net.uncontended.precipice.time.SystemTime;

public class RelaxedLatencyInterval<T extends Enum<T>> implements LatencyMetrics<T>, Interval<LatencyMetrics<T>> {

    private final Clock clock = new SystemTime();
    private final Class<T> clazz;
    private final LatencyFactory latencyFactory;
    private volatile LatencyMetrics<T> live;

    public RelaxedLatencyInterval(Class<T> clazz) {
        this(clazz, Latency.atomicHDRHistogram());
    }

    public RelaxedLatencyInterval(Class<T> clazz, LatencyFactory latencyFactory) {
        this.clazz = clazz;
        this.latencyFactory = latencyFactory;
        this.live = latencyFactory.newLatency(clazz, clock.nanoTime());
    }

    @Override
    public void record(T result, long number, long nanoLatency) {
        record(result, number, nanoLatency, clock.nanoTime());
    }

    @Override
    public void record(T result, long number, long nanoLatency, long nanoTime) {
        live.record(result, number, nanoLatency, nanoTime);
    }

    @Override
    public PrecipiceHistogram getHistogram(T metric) {
        return null;
    }

    @Override
    public Class<T> getMetricType() {
        return clazz;
    }

    @Override
    public LatencyMetrics<T> current() {
        return live;
    }

    @Override
    public synchronized LatencyMetrics<T> interval() {
        return interval(clock.nanoTime());
    }

    @Override
    public synchronized LatencyMetrics<T> interval(long nanoTime) {
        return interval(nanoTime, latencyFactory.newLatency(clazz, nanoTime));
    }

    @Override
    public synchronized LatencyMetrics<T> interval(long nanoTime, LatencyMetrics<T> newMetrics) {
        LatencyMetrics<T> oldLive = live;
        live = newMetrics;
        return oldLive;
    }
}