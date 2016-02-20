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

package net.uncontended.precipice.rate;

import net.uncontended.precipice.BackPressure;
import net.uncontended.precipice.Failable;
import net.uncontended.precipice.metrics.CountMetrics;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class RateLimiter<Rejected extends Enum<Rejected>> implements BackPressure<Rejected> {

    private final Rejected rejectedReason;
    private final long duration;
    private final long allowedPerPeriod;
    private final TimeUnit timeUnit;
    private final AtomicLong count = new AtomicLong(0);
    private volatile long rolloverTime;
    private final long nanoDuration;

    public RateLimiter(Rejected rejectedReason, long allowedPerPeriod, long duration, TimeUnit timeUnit) {
        this.rejectedReason = rejectedReason;
        this.allowedPerPeriod = allowedPerPeriod;
        this.duration = duration;
        this.timeUnit = timeUnit;
        this.nanoDuration = timeUnit.toNanos(duration);
        this.rolloverTime = System.nanoTime() + nanoDuration;
    }

    @Override
    public Rejected acquirePermit(long number, long nanoTime) {
        for (; ; ) {
            long currentCount = count.incrementAndGet();
            if (currentCount > allowedPerPeriod) {
                if (rolloverTime > nanoTime) {
                    return rejectedReason;
                } else if (count.compareAndSet(currentCount, 0)) {
                    rolloverTime = nanoTime;
                    return null;
                }
            } else {
                return null;
            }
        }
    }

    @Override
    public void releasePermit(long number, long nanoTime) {
    }

    @Override
    public void releasePermit(long number, Failable result, long nanoTime) {
    }

    @Override
    public <Result extends Enum<Result> & Failable> void registerResultMetrics(CountMetrics<Result> metrics) {

    }
}