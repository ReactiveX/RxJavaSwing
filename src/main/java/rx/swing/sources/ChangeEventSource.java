/**
 * Copyright 2014 Netflix, Inc.
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

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Note: There's no common supertype of all components which have add/removeChangeListener
 * methods, so we cannot implement add/removeListenerForComponent methods here, so this
 * class has to remain abstract.
 */
public abstract class ChangeEventSource extends EventSource<ChangeEvent, ChangeListener> {

    @Override
    public ChangeListener createListenerFor(final Subscriber<? super ChangeEvent> subscriber) {
        return new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                subscriber.onNext(e);
            }
        };
    }
}
