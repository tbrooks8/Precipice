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

import net.uncontended.precipice.concurrent.util.LongAdder;

public class IncrementCounter<T extends Enum<T>> implements CountMetrics<T> {

    private final LongAdder[] metrics;
    private final Class<T> clazz;

    public IncrementCounter(Class<T> clazz) {
        this.clazz = clazz;
        T[] metricValues = clazz.getEnumConstants();

        metrics = new LongAdder[metricValues.length];
        for (T metric : metricValues) {
            metrics[metric.ordinal()] = new LongAdder();
        }
    }

    @Override
    public void add(T metric, long delta) {
        metrics[metric.ordinal()].add(1L);
    }

    @Override
    public void add(T metric, long delta, long nanoTime) {
        metrics[metric.ordinal()].add(1L);
    }

    @Override
    public long getCount(T metric) {
        return metrics[metric.ordinal()].longValue();
    }

    @Override
    public Class<T> getMetricType() {
        return clazz;
    }

}