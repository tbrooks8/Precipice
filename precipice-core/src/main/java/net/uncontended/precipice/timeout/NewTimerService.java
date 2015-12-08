/*
 * Copyright 2015 Timothy Brooks
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
 */

package net.uncontended.precipice.timeout;

import java.util.concurrent.TimeUnit;

public class NewTimerService {

    private final long resolution;
    private final long startTime;

    public NewTimerService(long resolution, TimeUnit unit) {
        this.resolution = unit.toMillis(resolution);
        startTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
    }

    public void schedule(Object task, long delay, TimeUnit unit) {

    }
}