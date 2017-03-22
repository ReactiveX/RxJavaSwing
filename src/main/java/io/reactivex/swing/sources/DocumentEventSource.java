/**
 * Copyright 2015 Netflix, Inc.
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
package io.reactivex.swing.sources;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Cancellable;
import io.reactivex.observables.SwingObservable;
import io.reactivex.schedulers.SwingScheduler;

public enum DocumentEventSource { ; // no instances

    /**
     * @see SwingObservable#fromDocumentEvents(Document)
     */
    public static Observable<DocumentEvent> fromDocumentEventsOf(final Document document) {
        return Observable.create(new ObservableOnSubscribe<DocumentEvent>() {
            @Override
            public void subscribe(final ObservableEmitter<DocumentEvent> subscriber) {
                final DocumentListener listener = new DocumentListener() {
                    @Override
                    public void insertUpdate(DocumentEvent event) {
                        subscriber.onNext(event);
                    }

                    @Override
                    public void removeUpdate(DocumentEvent event) {
                        subscriber.onNext(event);
                    }

                    @Override
                    public void changedUpdate(DocumentEvent event) {
                        subscriber.onNext(event);
                    }
                };
                document.addDocumentListener(listener);
                subscriber.setCancellable(new Cancellable() {
                    @Override
                    public void cancel() {
                        document.removeDocumentListener(listener);
                    }
                });
            }
        }).subscribeOn(SwingScheduler.getInstance())
                .unsubscribeOn(SwingScheduler.getInstance());
    }
}
