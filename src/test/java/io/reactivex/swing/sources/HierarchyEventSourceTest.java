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
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.observables.SwingObservable;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;
import org.mockito.Mockito;

import javax.swing.*;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.util.Arrays;
import java.util.Collection;

import static org.mockito.Mockito.mock;

@RunWith(Parameterized.class)
public class HierarchyEventSourceTest {

    private final Function<Component, Observable<HierarchyEvent>> observableFactory;
    private JPanel rootPanel;
    private JPanel parentPanel;
    private Consumer<HierarchyEvent> action;
    private Consumer<Throwable> error;
    private Action complete;

    public HierarchyEventSourceTest(Function<Component, Observable<HierarchyEvent>> observableFactory) {
        this.observableFactory = observableFactory;
    }

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{{ObservablefromEventSource()},
                {ObservablefromSwingObservable()}});
    }

    private static Function<Component, Observable<HierarchyEvent>> ObservablefromEventSource() {
        return new Function<Component, Observable<HierarchyEvent>>() {
            @Override
            public Observable<HierarchyEvent> apply(Component component) {
                return HierarchyEventSource.fromHierarchyEventsOf(component);
            }
        };
    }

    private static Function<Component, Observable<HierarchyEvent>> ObservablefromSwingObservable() {
        return new Function<Component, Observable<HierarchyEvent>>() {
            @Override
            public Observable<HierarchyEvent> apply(Component component) {
                return SwingObservable.fromHierachyEvents(component);
            }
        };
    }

    @SuppressWarnings("unchecked")
    @Before
    public void setup() {
        rootPanel = new JPanel();
        parentPanel = new JPanel();

        action = mock(Consumer.class);
        error = mock(Consumer.class);
        complete = mock(Action.class);
    }

    @Test
    public void testObservingHierarchyEvents() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(new Action() {
            @Override
            public void run() throws Exception {
                JPanel childPanel = Mockito.spy(new JPanel());
                parentPanel.add(childPanel);

                Disposable subscription = observableFactory.apply(childPanel)
                        .subscribe(action, error, complete);

                rootPanel.add(parentPanel);

                Mockito.verify(action).accept(Matchers.argThat(hierarchyEventMatcher(childPanel, HierarchyEvent.PARENT_CHANGED, parentPanel, rootPanel)));
                Mockito.verify(error, Mockito.never()).accept(Mockito.any(Throwable.class));
                Mockito.verify(complete, Mockito.never()).run();

                // Verifies that the underlying listener has been removed.
                subscription.dispose();
                Mockito.verify(childPanel).removeHierarchyListener(Mockito.any(HierarchyListener.class));
                Assert.assertEquals(0, childPanel.getHierarchyListeners().length);

                // Sanity check to verify that no more events are emitted after unsubscribing.
                rootPanel.remove(parentPanel);
                Mockito.verifyNoMoreInteractions(action, error, complete);
            }
        }).awaitTerminal();
    }

    private ArgumentMatcher<HierarchyEvent> hierarchyEventMatcher(final Component source, final int changeFlags, final Container changed, final Container changedParent) {
        return new ArgumentMatcher<HierarchyEvent>() {
            @Override
            public boolean matches(HierarchyEvent event) {
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
