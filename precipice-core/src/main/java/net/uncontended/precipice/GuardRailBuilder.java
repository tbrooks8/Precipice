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
import net.uncontended.precipice.metrics.latency.WritableLatency;
import net.uncontended.precipice.time.Clock;

public class GuardRailBuilder<Result extends Enum<Result> & Failable, Rejected extends Enum<Rejected>> {

    private GuardRailProperties<Result, Rejected> properties = new GuardRailProperties<>();


    public GuardRailBuilder<Result, Rejected> addBackPressure(BackPressure<Rejected> backPressure) {
        this.properties.backPressureList.add(backPressure);
        return this;
    }

    public GuardRailBuilder<Result, Rejected> name(String name) {
        this.properties.name = name;
        return this;
    }

    public GuardRailBuilder<Result, Rejected> resultMetrics(WritableCounts<Result> resultMetrics) {
        this.properties.resultMetrics.add(resultMetrics);
        return this;
    }

    public GuardRailBuilder<Result, Rejected> rejectedMetrics(WritableCounts<Rejected> rejectedMetrics) {
        this.properties.rejectedMetrics.add(rejectedMetrics);
        return this;
    }

    public GuardRailBuilder<Result, Rejected> resultLatency(WritableLatency<Result> resultLatency) {
        this.properties.resultLatency.add(resultLatency);
        return this;
    }

    public GuardRailBuilder<Result, Rejected> clock(Clock clock) {
        this.properties.clock = clock;
        return this;
    }

    public GuardRail<Result, Rejected> build() {
        if (properties.name == null) {
            throw new IllegalArgumentException();
        } else if (properties.resultMetrics == null) {
            throw new IllegalArgumentException();
        } else if (properties.rejectedMetrics == null) {
            throw new IllegalArgumentException();
        }

        return GuardRail.create(properties);
    }
}
