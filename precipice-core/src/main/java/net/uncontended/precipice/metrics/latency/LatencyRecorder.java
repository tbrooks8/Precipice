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

package net.uncontended.precipice.metrics.latency;

import net.uncontended.precipice.metrics.AbstractMetrics;
import net.uncontended.precipice.metrics.tools.Recorder;
import net.uncontended.precipice.metrics.tools.RelaxedRecorder;

public class LatencyRecorder<T extends Enum<T>> extends AbstractMetrics<T> implements WritableLatency<T>, Capturer<PartitionedLatency<T>> {

    private final Recorder<PartitionedLatency<T>> recorder;
    private PartitionedLatency<T> inactive;

    public LatencyRecorder(PartitionedLatency<T> active, PartitionedLatency<T> inactive) {
        this(active, inactive, new RelaxedRecorder<PartitionedLatency<T>>());
    }

    public LatencyRecorder(PartitionedLatency<T> active, PartitionedLatency<T> inactive, Recorder<PartitionedLatency<T>> recorder) {
        super(active.getMetricClazz());
        this.recorder = recorder;
        this.recorder.flip(active);
        this.inactive = inactive;
    }

    @Override
    public void write(T result, long number, long nanoLatency, long nanoTime) {
        long permit = recorder.startRecord();
        try {
            recorder.active().record(result, number, nanoLatency);
        } finally {
            recorder.endRecord(permit);
        }
    }

    @Override
    public synchronized PartitionedLatency<T> captureInterval() {
        inactive.reset();
        PartitionedLatency<T> newlyInactive = recorder.flip(inactive);
        inactive = newlyInactive;
        return newlyInactive;
    }

    @Override
    public synchronized PartitionedLatency<T> captureInterval(PartitionedLatency<T> newLatency) {
        PartitionedLatency<T> newlyInactive = recorder.flip(newLatency);
        inactive = newlyInactive;
        return newlyInactive;
    }

    public static <T extends Enum<T>> LatencyRecorderBuilder<T> builder(Class<T> clazz) {
        return new LatencyRecorderBuilder<>(clazz);
    }
}
