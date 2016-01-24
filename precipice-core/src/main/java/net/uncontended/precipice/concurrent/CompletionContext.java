/*
 * Copyright 2014 Timothy Brooks
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

package net.uncontended.precipice.concurrent;

import net.uncontended.precipice.PerformingContext;
import net.uncontended.precipice.PrecipiceFunction;
import net.uncontended.precipice.Result;

public class CompletionContext<S extends Result, T> implements Completable<S, T>, PerformingContext {

    private final long startTime;
    private PrecipiceFunction<S, PerformingContext> internalCallback;
    private boolean isCompleted = false;
    private T result;
    private Throwable error;

    public CompletionContext() {
        this(System.nanoTime());
    }

    public CompletionContext(long startTime) {
        this.startTime = startTime;
    }

    @Override
    public long startNanos() {
        return startTime;
    }

    @Override
    public boolean complete(S status, T result) {
        if (!this.isCompleted) {
            internalCallback.apply(status, this);
            return true;
        }
        return false;
    }

    @Override
    public boolean completeExceptionally(S status, Throwable ex) {
        if (!this.isCompleted) {
            internalCallback.apply(status, this);
            return true;
        }
        return false;
    }

    public void internalOnComplete(PrecipiceFunction<S, PerformingContext> fn) {
        internalCallback = fn;
    }
}