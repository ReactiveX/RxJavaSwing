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
package rx.swing.sources;

import rx.Subscriber;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class ItemEventSource extends EventSource<ItemEvent, ItemListener> {

    private ItemSelectable component;

    public ItemEventSource(ItemSelectable component) {
        super();
        this.component = component;
    }

    @Override
    protected ItemListener createListenerFor(final Subscriber<? super ItemEvent> subscriber) {
        return new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent event) {
                subscriber.onNext(event);
            }
        };
    }

    @Override
    protected void addListenerToComponent(ItemListener listener) {
        component.addItemListener(listener);
    }

    @Override
    protected void removeListenerFromComponent(ItemListener listener) {
        component.removeItemListener(listener);
    }
}
