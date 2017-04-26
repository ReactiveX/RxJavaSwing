/**
 * Copyright 2015 Netflix
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
import io.reactivex.observables.SwingObservable;
import io.reactivex.schedulers.SwingScheduler;

import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public enum WindowEventSource {
    ; // no instances

    /**
     * @see SwingObservable#fromWindowEventsOf(Window)
     */
    public static Observable<WindowEvent> fromWindowEventsOf(final Window window) {
        return Observable.create(new ObservableOnSubscribe<WindowEvent>() {
            @Override
            public void subscribe(final ObservableEmitter<WindowEvent> subscriber) {
                final WindowListener windowListener = new WindowListener() {
                    @Override
                    public void windowOpened(WindowEvent windowEvent) {
                        subscriber.onNext(windowEvent);
                    }

                    @Override
                    public void windowClosing(WindowEvent windowEvent) {
                        subscriber.onNext(windowEvent);
                    }

                    @Override
                    public void windowClosed(WindowEvent windowEvent) {
                        subscriber.onNext(windowEvent);
                    }

                    @Override
                    public void windowIconified(WindowEvent windowEvent) {
                        subscriber.onNext(windowEvent);
                    }

                    @Override
                    public void windowDeiconified(WindowEvent windowEvent) {
                        subscriber.onNext(windowEvent);
                    }

                    @Override
                    public void windowActivated(WindowEvent windowEvent) {
                        subscriber.onNext(windowEvent);
                    }

                    @Override
                    public void windowDeactivated(WindowEvent windowEvent) {
                        subscriber.onNext(windowEvent);
                    }
                };

                window.addWindowListener(windowListener);

                subscriber.setCancellable(new Cancellable() {
                    @Override
                    public void cancel() {
                        window.removeWindowListener(windowListener);
                    }
                });
            }
        }).subscribeOn(SwingScheduler.getInstance())
                .unsubscribeOn(SwingScheduler.getInstance());
    }
}
