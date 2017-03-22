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
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Cancellable;
import io.reactivex.observables.SwingObservable;
import io.reactivex.schedulers.SwingScheduler;

import java.awt.*;
import java.awt.event.*;

public enum MouseEventSource {
    ; // no instances

    /**
     * @see SwingObservable#fromMouseEvents
     */
    public static Observable<MouseEvent> fromMouseEventsOf(final Component component) {
        return Observable.create(new ObservableOnSubscribe<MouseEvent>() {
            @Override
            public void subscribe(final ObservableEmitter<MouseEvent> subscriber) {
                final MouseListener listener = new MouseListener() {
                    @Override
                    public void mouseClicked(MouseEvent event) {
                        subscriber.onNext(event);
                    }

                    @Override
                    public void mousePressed(MouseEvent event) {
                        subscriber.onNext(event);
                    }

                    @Override
                    public void mouseReleased(MouseEvent event) {
                        subscriber.onNext(event);
                    }

                    @Override
                    public void mouseEntered(MouseEvent event) {
                        subscriber.onNext(event);
                    }

                    @Override
                    public void mouseExited(MouseEvent event) {
                        subscriber.onNext(event);
                    }
                };
                component.addMouseListener(listener);

                subscriber.setCancellable(new Cancellable() {
                    @Override
                    public void cancel() {
                        component.removeMouseListener(listener);
                    }
                });
            }
        }).subscribeOn(SwingScheduler.getInstance())
                .unsubscribeOn(SwingScheduler.getInstance());
    }

    /**
     * @see SwingObservable#fromMouseMotionEvents
     */
    public static Observable<MouseEvent> fromMouseMotionEventsOf(final Component component) {
        return Observable.create(new ObservableOnSubscribe<MouseEvent>() {
            @Override
            public void subscribe(final ObservableEmitter<MouseEvent> subscriber) {
                final MouseMotionListener listener = new MouseMotionListener() {
                    @Override
                    public void mouseDragged(MouseEvent event) {
                        subscriber.onNext(event);
                    }

                    @Override
                    public void mouseMoved(MouseEvent event) {
                        subscriber.onNext(event);
                    }
                };
                component.addMouseMotionListener(listener);

                subscriber.setCancellable(new Cancellable() {
                    @Override
                    public void cancel() {
                        component.removeMouseMotionListener(listener);
                    }
                });
            }
        }).subscribeOn(SwingScheduler.getInstance())
                .unsubscribeOn(SwingScheduler.getInstance());
    }

    public static Observable<MouseWheelEvent> fromMouseWheelEvents(final Component component) {
        return Observable.create(new ObservableOnSubscribe<MouseWheelEvent>() {
            @Override
            public void subscribe(final ObservableEmitter<MouseWheelEvent> subscriber) {
                final MouseWheelListener listener = new MouseWheelListener() {
                    @Override
                    public void mouseWheelMoved(MouseWheelEvent event) {
                        subscriber.onNext(event);
                    }
                };
                component.addMouseWheelListener(listener);

                subscriber.setCancellable(new Cancellable() {
                    @Override
                    public void cancel() {
                        component.removeMouseWheelListener(listener);
                    }
                });
            }
        }).subscribeOn(SwingScheduler.getInstance())
                .unsubscribeOn(SwingScheduler.getInstance());
    }

    /**
     * @see SwingObservable#fromRelativeMouseMotion
     */
    public static Observable<Point> fromRelativeMouseMotion(final Component component) {
        final Observable<MouseEvent> events = fromMouseMotionEventsOf(component);
        return Observable.zip(events, events.skip(1), new BiFunction<MouseEvent, MouseEvent, Point>() {
            @Override
            public Point apply(MouseEvent ev1, MouseEvent ev2) {
                return new Point(ev2.getX() - ev1.getX(), ev2.getY() - ev1.getY());
            }
        }).subscribeOn(SwingScheduler.getInstance())
                .unsubscribeOn(SwingScheduler.getInstance());
    }

}
