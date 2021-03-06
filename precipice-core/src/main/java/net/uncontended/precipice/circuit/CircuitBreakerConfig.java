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

import java.util.concurrent.TimeUnit;

public class CircuitBreakerConfig<Rejected extends Enum<Rejected>> {

    public final Rejected reason;
    public final Rejected forcedReason;
    public final int failurePercentageThreshold;
    public final long failureThreshold;
    public final long sampleSizeThreshold;
    public final long trailingPeriodNanos;
    public final long healthRefreshNanos;
    public final long backOffTimeNanos;

    public CircuitBreakerConfig(Rejected reason, Rejected forcedReason, long failureThreshold, int failurePercentageThreshold,
                                long sampleSizeThreshold, long trailingPeriodMillis, long healthRefreshMillis, long backOffTimeMillis) {
        this.reason = reason;
        this.forcedReason = forcedReason;
        this.failureThreshold = failureThreshold;
        this.failurePercentageThreshold = failurePercentageThreshold;
        this.sampleSizeThreshold = sampleSizeThreshold;
        trailingPeriodNanos = TimeUnit.MILLISECONDS.toNanos(trailingPeriodMillis);
        healthRefreshNanos = TimeUnit.MILLISECONDS.toNanos(healthRefreshMillis);
        backOffTimeNanos = TimeUnit.MILLISECONDS.toNanos(backOffTimeMillis);
    }
}
