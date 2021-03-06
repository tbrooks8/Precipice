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

package net.uncontended.precipice.circuit;

import net.uncontended.precipice.Failable;
import net.uncontended.precipice.metrics.IntervalIterator;
import net.uncontended.precipice.metrics.Rolling;
import net.uncontended.precipice.metrics.counts.PartitionedCount;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

public class HealthGauge {

    private final CopyOnWriteArrayList<InternalGauge<?>> gauges = new CopyOnWriteArrayList<>();

    public synchronized HealthSnapshot getHealth(long timePeriod, TimeUnit timeUnit, long nanoTime) {
        long total = 0;
        long failures = 0;

        for (InternalGauge<?> gauge : gauges) {
            gauge.refreshHealth(timePeriod, timeUnit, nanoTime);
            total += gauge.total;
            failures += gauge.failures;
        }
        return new HealthSnapshot(total, failures);
    }

    public <Result extends Enum<Result> & Failable> void add(Rolling<PartitionedCount<Result>> metrics) {
        gauges.add(new InternalGauge<>(metrics));
    }

    private static class InternalGauge<Result extends Enum<Result> & Failable> {

        private final Rolling<PartitionedCount<Result>> metrics;
        private final Class<Result> type;
        private long total = 0;
        private long failures = 0;

        private InternalGauge(Rolling<PartitionedCount<Result>> metrics) {
            this.metrics = metrics;
            type = metrics.current().getMetricClazz();
        }

        private void refreshHealth(long timePeriod, TimeUnit timeUnit, long nanoTime) {
            total = 0;
            failures = 0;
            IntervalIterator<PartitionedCount<Result>> counters = metrics.intervals(nanoTime);
            counters.limit(timePeriod, timeUnit);

            PartitionedCount<Result> metricCounter;
            while (counters.hasNext()) {
                metricCounter = counters.next();
                for (Result result : type.getEnumConstants()) {
                    long metricCount = metricCounter.getCount(result);
                    total += metricCount;

                    if (result.isFailure()) {
                        failures += metricCount;
                    }
                }
            }
        }
    }
}
