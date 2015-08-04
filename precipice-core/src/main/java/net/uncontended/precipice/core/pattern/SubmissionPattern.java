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

package net.uncontended.precipice.core.pattern;

import net.uncontended.precipice.core.RejectedActionException;
import net.uncontended.precipice.core.concurrent.PrecipiceFuture;
import net.uncontended.precipice.core.concurrent.Promise;

public interface SubmissionPattern<C> extends Pattern<C> {
    /**
     * Submits a {@link ResilientPatternAction} that will be run asynchronously.
     * The result of the action will be delivered to the future returned
     * by this call. An attempt to cancel the action will be made if it
     * does not complete before the timeout.
     *
     * @param action        the action to complete
     * @param millisTimeout milliseconds before the action times out
     * @param <T>           the type of the result of the action
     * @return a {@link PrecipiceFuture} representing pending completion of the action
     * @throws RejectedActionException if the action is rejected
     */
    <T> PrecipiceFuture<T> complete(ResilientPatternAction<T, C> action, long millisTimeout);

    <T> void complete(ResilientPatternAction<T, C> action, Promise<T> promise, long millisTimeout);

}
