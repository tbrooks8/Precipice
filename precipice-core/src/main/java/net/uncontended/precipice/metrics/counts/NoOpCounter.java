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

package net.uncontended.precipice.metrics.counts;

import net.uncontended.precipice.metrics.AbstractMetrics;
import net.uncontended.precipice.metrics.PartitionedCount;

public class NoOpCounter<T extends Enum<T>> extends AbstractMetrics<T> implements PartitionedCount<T> {

    public NoOpCounter(Class<T> clazz) {
        super(clazz);
    }

    @Override
    public void add(T metric, long delta) {
    }

    @Override
    public void add(T metric, long delta, long nanoTime) {
    }

    @Override
    public long getCount(T metric) {
        return 0;
    }

    @Override
    public long total() {
        return 0;
    }

    @Override
    public void reset() {

    }
}