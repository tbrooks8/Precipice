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

package net.uncontended.precipice.semaphore;

import net.uncontended.precipice.BackPressure;
import net.uncontended.precipice.Failable;
import net.uncontended.precipice.GuardRail;

import java.util.concurrent.atomic.AtomicLong;

public class UnlimitedSemaphore<Rejected extends Enum<Rejected>> implements BackPressure<Rejected>, PrecipiceSemaphore {

    private final AtomicLong concurrencyLevel = new AtomicLong(0);

    @Override
    public Rejected acquirePermit(long number, long nanoTime) {
        concurrencyLevel.addAndGet(number);
        return null;
    }

    @Override
    public void releasePermit(long number, long nanoTime) {
        concurrencyLevel.addAndGet(-number);
    }


    @Override
    public void releasePermit(long number, Failable result, long nanoTime) {
        concurrencyLevel.addAndGet(-number);
    }

    @Override
    public <Result extends Enum<Result> & Failable> void registerGuardRail(GuardRail<Result, Rejected> guardRail) {
    }


    @Override
    public long maxConcurrencyLevel() {
        return -1;
    }

    @Override
    public long remainingCapacity() {
        return -1;
    }

    @Override
    public long currentConcurrencyLevel() {
        return concurrencyLevel.get();
    }
}
