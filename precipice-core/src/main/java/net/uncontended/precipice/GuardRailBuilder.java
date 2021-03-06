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

package net.uncontended.precipice;

import net.uncontended.precipice.metrics.counts.WritableCounts;
import net.uncontended.precipice.metrics.latency.NoOpLatency;
import net.uncontended.precipice.metrics.latency.WritableLatency;
import net.uncontended.precipice.time.Clock;

public class GuardRailBuilder<Result extends Enum<Result> & Failable, Rejected extends Enum<Rejected>> {

    private final GuardRailProperties<Result, Rejected> properties = new GuardRailProperties<>();


    public GuardRailBuilder<Result, Rejected> name(String name) {
        properties.name = name;
        return this;
    }

    public GuardRailBuilder<Result, Rejected> addBackPressure(BackPressure<Rejected> backPressure) {
        properties.backPressureMap.put(Integer.toString(properties.backPressureMap.size()), backPressure);
        return this;
    }

    public GuardRailBuilder<Result, Rejected> addBackPressure(String name, BackPressure<Rejected> backPressure) {
        properties.backPressureMap.put(name, backPressure);
        return this;
    }

    public GuardRailBuilder<Result, Rejected> resultCounts(WritableCounts<Result> resultCounts) {
        properties.resultCounts = resultCounts;
        return this;
    }

    public GuardRailBuilder<Result, Rejected> rejectedCounts(WritableCounts<Rejected> rejectedCounts) {
        properties.rejectedCounts = rejectedCounts;
        return this;
    }

    public GuardRailBuilder<Result, Rejected> resultLatency(WritableLatency<Result> resultLatency) {
        properties.resultLatency = resultLatency;
        return this;
    }

    public GuardRailBuilder<Result, Rejected> clock(Clock clock) {
        properties.clock = clock;
        return this;
    }

    public GuardRail<Result, Rejected> build() {
        if (properties.name == null) {
            throw new IllegalArgumentException("Name is required.");
        } else if (properties.resultCounts == null) {
            throw new IllegalArgumentException("Result counts are required.");
        } else if (properties.rejectedCounts == null) {
            throw new IllegalArgumentException("Rejected counts are required.");
        }

        if (properties.resultLatency == null) {
            properties.resultLatency = new NoOpLatency<>(properties.resultCounts.getMetricClazz());
        }

        return GuardRail.create(properties);
    }
}
