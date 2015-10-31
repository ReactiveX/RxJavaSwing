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

import static org.mockito.Mockito.mock;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

import javax.swing.JPanel;

import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;
import org.mockito.Mockito;

import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;

public class HierarchyEventSourceTest {
    @Test
    public void testObservingHierarchyEvents() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(new Action0() {
            @Override
            public void call() {
                JPanel rootPanel = new JPanel();
                JPanel parentPanel = new JPanel();

                JPanel childPanel = Mockito.spy(new JPanel());
                parentPanel.add(childPanel);

                @SuppressWarnings("unchecked")
                Action1<HierarchyEvent> action = mock(Action1.class);
                @SuppressWarnings("unchecked")
                Action1<Throwable> error = mock(Action1.class);
                Action0 complete = mock(Action0.class);

                Subscription subscription = 
                    HierarchyEventSource.fromHierarchyEventsOf(childPanel)
                                        .subscribe(action, error, complete);

                rootPanel.add(parentPanel);

                Mockito.verify(action).call(Matchers.argThat(hierarchyEventMatcher(childPanel, HierarchyEvent.PARENT_CHANGED, parentPanel, rootPanel)));
                Mockito.verify(error, Mockito.never()).call(Mockito.any(Throwable.class));
                Mockito.verify(complete, Mockito.never()).call();

                // Verifies that the underlying listener has been removed.
                subscription.unsubscribe();
                Mockito.verify(childPanel).removeHierarchyListener(Mockito.any(HierarchyListener.class));
                Assert.assertEquals(0, childPanel.getHierarchyListeners().length);

                // Sanity check to verify that no more events are emitted after unsubscribing.
                rootPanel.remove(parentPanel);
                Mockito.verifyNoMoreInteractions(action, error, complete);
            }
        }).awaitTerminal();
    }

    private Matcher<HierarchyEvent> hierarchyEventMatcher(final Component source, final int changeFlags, final Container changed, final Container changedParent) {
        return new ArgumentMatcher<HierarchyEvent>() {
            @Override
            public boolean matches(Object argument) {
                if (argument.getClass() != HierarchyEvent.class)
                    return false;

                HierarchyEvent event = (HierarchyEvent) argument;

                if (source != event.getComponent())
                    return false;

                if (changed != event.getChanged())
                    return false;

                if (changedParent != event.getChangedParent())
                    return false;

                return changeFlags == event.getChangeFlags();
            }
        };
    }
}
