/**
 * Copyright 2015 Netflix, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.reactivex.swing.sources;


import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposables;
import io.reactivex.functions.Action;
import io.reactivex.functions.Cancellable;
import io.reactivex.schedulers.SwingScheduler;

import java.awt.*;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;

public enum ContainerEventSource {
    ; // no instances

    /**
     * @see io.reactivex.observables.SwingObservable#fromContainerEvents
     */
    public static Observable<ContainerEvent> fromContainerEventsOf(final Container container) {
        return Observable.create(new ObservableOnSubscribe<ContainerEvent>() {

            @Override
            public void subscribe(final ObservableEmitter<ContainerEvent> subscriber) throws Exception {
                final ContainerListener listener = new ContainerListener() {
                    @Override
                    public void componentRemoved(ContainerEvent event) {
                        subscriber.onNext(event);
                    }

                    @Override
                    public void componentAdded(ContainerEvent event) {
                        subscriber.onNext(event);
                    }
                };
                container.addContainerListener(listener);
                subscriber.setCancellable(new Cancellable() {
                    @Override
                    public void cancel() {
                        container.removeContainerListener(listener);
                    }
                });

            }
        }).subscribeOn(SwingScheduler.getInstance()).observeOn(SwingScheduler.getInstance()).unsubscribeOn(SwingScheduler.getInstance());
    }

    public static enum Predicate implements io.reactivex.functions.Predicate<ContainerEvent> {
        COMPONENT_ADDED(ContainerEvent.COMPONENT_ADDED), COMPONENT_REMOVED(ContainerEvent.COMPONENT_REMOVED);

        private final int id;

        private Predicate(int id) {
            this.id = id;
        }

        @Override
        public boolean test(ContainerEvent event) {
            return event.getID() == id;
        }
    }
}
