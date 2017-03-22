/**
 * Copyright 2014 Netflix, Inc.
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
import io.reactivex.functions.Cancellable;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.observables.SwingObservable;
import io.reactivex.schedulers.SwingScheduler;

import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import static io.reactivex.swing.sources.ComponentEventSource.Predicate.RESIZED;

public enum ComponentEventSource {
    ; // no instances

    /**
     * @see SwingObservable#fromComponentEvents
     */
    public static Observable<ComponentEvent> fromComponentEventsOf(final Component component) {
        return Observable.create(new ObservableOnSubscribe<ComponentEvent>() {
            @Override
            public void subscribe(final ObservableEmitter<ComponentEvent> subscriber) {
                final ComponentListener listener = new ComponentListener() {
                    @Override
                    public void componentHidden(ComponentEvent event) {
                        subscriber.onNext(event);
                    }

                    @Override
                    public void componentMoved(ComponentEvent event) {
                        subscriber.onNext(event);
                    }

                    @Override
                    public void componentResized(ComponentEvent event) {
                        subscriber.onNext(event);
                    }

                    @Override
                    public void componentShown(ComponentEvent event) {
                        subscriber.onNext(event);
                    }
                };
                component.addComponentListener(listener);
                subscriber.setCancellable(new Cancellable() {
                    @Override
                    public void cancel() {
                        component.removeComponentListener(listener);
                    }
                });
            }
        }).subscribeOn(SwingScheduler.getInstance())
                .unsubscribeOn(SwingScheduler.getInstance());
    }

    /**
     * @see SwingObservable#fromResizing
     */
    public static Observable<Dimension> fromResizing(final Component component) {
        return fromComponentEventsOf(component).filter(RESIZED).map(new Function<ComponentEvent, Dimension>() {
            @Override
            public Dimension apply(ComponentEvent event) {
                return event.getComponent().getSize();
            }
        });
    }

    /**
     * Predicates that help with filtering observables for specific component events. 
     */
    public enum Predicate implements io.reactivex.functions.Predicate<ComponentEvent> {
        RESIZED(ComponentEvent.COMPONENT_RESIZED),
        HIDDEN(ComponentEvent.COMPONENT_HIDDEN),
        MOVED(ComponentEvent.COMPONENT_MOVED),
        SHOWN(ComponentEvent.COMPONENT_SHOWN);

        private final int id;

        private Predicate(int id) {
            this.id = id;
        }

        @Override
        public boolean test(ComponentEvent event) {
            return event.getID() == id;
        }
    }
}
