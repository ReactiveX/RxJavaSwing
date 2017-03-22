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
import io.reactivex.functions.Cancellable;
import io.reactivex.schedulers.SwingScheduler;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public enum ItemEventSource {
    ; // no instances

    public static Observable<ItemEvent> fromItemEventsOf(final ItemSelectable itemSelectable) {
        return Observable.create(new ObservableOnSubscribe<ItemEvent>() {
            @Override
            public void subscribe(final ObservableEmitter<ItemEvent> subscriber) {
                final ItemListener listener = new ItemListener() {
                    @Override
                    public void itemStateChanged(ItemEvent event) {
                        subscriber.onNext(event);
                    }
                };
                itemSelectable.addItemListener(listener);
                subscriber.setCancellable(new Cancellable() {
                    @Override
                    public void cancel() {
                        itemSelectable.removeItemListener(listener);
                    }
                });
            }
        }).subscribeOn(SwingScheduler.getInstance())
                .unsubscribeOn(SwingScheduler.getInstance());
    }
}
