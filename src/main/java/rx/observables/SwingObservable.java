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
package rx.observables;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.text.Document;

import rx.Observable;
import rx.Subscriber;
import rx.Observable.OnSubscribe;
import rx.functions.Func1;
import rx.schedulers.SwingScheduler;
import rx.swing.sources.*;

/**
 * Allows creating observables from various sources specific to Swing. 
 */
public enum SwingObservable { ; // no instances

    private static <T> Observable<T> create(OnSubscribe<T> f) {
        return Observable.create(f)
                .subscribeOn(SwingScheduler.getInstance())
                .unsubscribeOn(SwingScheduler.getInstance());
    }

    /**
     * Creates an observable corresponding to a Swing button action.
     * 
     * @param button 
     *            The button to register the observable for.
     * @return Observable of action events.
     */
    public static Observable<ActionEvent> fromButtonAction(final AbstractButton button) {
        return create(new ActionEventSource() {
            @Override
            public void addListenerToComponent(ActionListener listener) {
                button.addActionListener(listener);
            }
            @Override
            public void removeListenerFromComponent(ActionListener listener) {
                button.removeActionListener(listener);
            }
        });
    }

    /**
     * Creates an observable corresponding to raw key events.
     * 
     * @param component
     *            The component to register the observable for.
     * @return Observable of key events.
     */
    public static Observable<KeyEvent> fromKeyEvents(Component component) {
        return create(new KeyEventSource(component));
    }

    /**
     * Creates an observable corresponding to raw key events, restricted a set of given key codes.
     * 
     * @param component
     *            The component to register the observable for.
     * @return Observable of key events.
     */
    public static Observable<KeyEvent> fromKeyEvents(Component component, final Set<Integer> keyCodes) {
        return fromKeyEvents(component).filter(new Func1<KeyEvent, Boolean>() {
            @Override
            public Boolean call(KeyEvent event) {
                return keyCodes.contains(event.getKeyCode());
            }
        });
    }

    /**
     * Creates an observable that emits the set of all currently pressed keys each time
     * this set changes. 
     * @param component
     *            The component to register the observable for.
     * @return Observable of currently pressed keys.
     */
    public static Observable<Set<Integer>> fromPressedKeys(Component component) {
        return KeyEventSource.currentlyPressedKeys(fromKeyEvents(component));
    }

    /**
     * Creates an observable corresponding to raw mouse events (excluding mouse motion events).
     * 
     * @param component
     *            The component to register the observable for.
     * @return Observable of mouse events.
     */
    public static Observable<MouseEvent> fromMouseEvents(Component component) {
        return MouseEventSource.fromMouseEventsOf(component);
    }

    /**
     * Creates an observable corresponding to raw mouse motion events.
     * 
     * @param component
     *            The component to register the observable for.
     * @return Observable of mouse motion events.
     */
    public static Observable<MouseEvent> fromMouseMotionEvents(Component component) {
        return MouseEventSource.fromMouseMotionEventsOf(component);
    }
    
    /**
     * Creates an observable corresponding to relative mouse motion.
     * @param component
     *            The component to register the observable for.
     * @return A point whose x and y coordinate represent the relative horizontal and vertical mouse motion.
     */
    public static Observable<Point> fromRelativeMouseMotion(Component component) {
        return MouseEventSource.fromRelativeMouseMotion(component);
    }

    /**
     * Creates an observable corresponding to raw mouse wheel events.
     *
     * @param component
     *            The component to register the observable for.
     * @return The component to register the observable for.
     */
    public static Observable<MouseWheelEvent> fromMouseWheelEvents(Component component) {
        return MouseEventSource.fromMouseWheelEvents(component);
    }
    
    /**
     * Creates an observable corresponding to raw component events.
     * 
     * @param component
     *            The component to register the observable for.
     * @return Observable of component events.
     */
    public static Observable<ComponentEvent> fromComponentEvents(Component component) {
        return create(new ComponentEventSource(component));
    }

    /**
     * Creates an observable corresponding to focus events.
     *
     * @param component
     *            The component to register the observable for.
     * @return Observable of focus events.
     */
    public static Observable<FocusEvent> fromFocusEvents(Component component) {
        return FocusEventSource.fromFocusEventsOf(component);
    }

    /**
     * Creates an observable corresponding to component resize events.
     * 
     * @param component
     *            The component to register the observable for.
     * @return Observable emitting the current size of the given component after each resize event.
     */
    public static Observable<Dimension> fromResizing(Component component) {
        return ComponentEventSource.resizing(fromComponentEvents(component));
    }

    /**
     * Creates an observable corresponding to item events.
     * 
     * @param itemSelectable
     *            The ItemSelectable to register the observable for.
     * @return Observable emitting the item events for the given itemSelectable.
     */
    public static Observable<ItemEvent> fromItemEvents(ItemSelectable itemSelectable) {
        return create(new ItemEventSource(itemSelectable));
    }
    
    /**
     * Creates an observable corresponding to item selection events.
     * 
     * @param itemSelectable
     *            The ItemSelectable to register the observable for.
     * @return Observable emitting the an item event whenever the given itemSelectable is selected.
     */
    public static Observable<ItemEvent> fromItemSelectionEvents(ItemSelectable itemSelectable) {
        return fromItemEvents(itemSelectable).filter(new Func1<ItemEvent, Boolean>() {
            @Override
            public Boolean call(ItemEvent event) {
                return event.getStateChange() == ItemEvent.SELECTED;
            }
        });
    }
    
    /**
     * Creates an observable corresponding to item deselection events.
     * 
     * @param itemSelectable
     *            The ItemSelectable to register the observable for.
     * @return Observable emitting the an item event whenever the given itemSelectable is deselected.
     */
    public static Observable<ItemEvent> fromItemDeselectionEvents(ItemSelectable itemSelectable) {
        return fromItemEvents(itemSelectable).filter(new Func1<ItemEvent, Boolean>() {
            @Override
            public Boolean call(ItemEvent event) {
                return event.getStateChange() == ItemEvent.DESELECTED;
            }
        });
    }

    /**
     * Creates an observable corresponding to list selection events (e.g. from a JList or a JTable row / column selection).
     *
     * For more info to swing list selection see <a href="https://docs.oracle.com/javase/tutorial/uiswing/events/listselectionlistener.html">
	 * How to Write a List Selection Listener</a>.
     *
     * @param listSelectionModel
     *            The ListSelectionModel to register the observable for.
     * @return Observable emitting the list selection events.
     */
    public static Observable<ListSelectionEvent> fromListSelectionEvents(ListSelectionModel listSelectionModel) {
        return ListSelectionEventSource.fromListSelectionEventsOf(listSelectionModel);
    }
    
    /**
     * Creates an observable corresponding to property change events.
     * 
     * @param component
     *            The component to register the observable for.
     * @return Observable of property change events for the given component
     */
    public static Observable<PropertyChangeEvent> fromPropertyChangeEvents(Component component) {
        return PropertyChangeEventSource.fromPropertyChangeEventsOf(component);
    }
    
    /**
     * Creates an observable corresponding to property change events filtered by property name.
     * 
     * @param component
     *            The component to register the observable for.
     * @param propertyName
     *            A property name to filter the property events on.
     * @return Observable of property change events for the given component, filtered by the provided property name
     */
    public static Observable<PropertyChangeEvent> fromPropertyChangeEvents(Component component, final String propertyName) {
        return fromPropertyChangeEvents(component).filter(new Func1<PropertyChangeEvent, Boolean>() {
            @Override
            public Boolean call(PropertyChangeEvent event) {
                return event.getPropertyName().equals(propertyName);
            }
        });
    }

    /**
     * @param window
     *      The window to register the observable for
     * @return Observable of window events for the given window
     */
    public static Observable<WindowEvent> fromWindowEventsOf(Window window) {
        return WindowEventSource.fromWindowEventsOf(window);
    }

    /**
     * Creates an observable corresponding to document events.
     *
     * @param document The document to register the observable for.
     * @return Observable of document events.
     */
    public static Observable<DocumentEvent> fromDocumentEvents(Document document) {
        return DocumentEventSource.fromDocumentEventsOf(document);
    }

    /**
     * Creates an observable corresponding to document events restricted to a
     * set of given event types.
     *
     * @param document The document to register the observable for.
     * @param eventTypes The set of event types for which the observable should
     * emit document events.
     * @return Observable of document events.
     */
    public static Observable<DocumentEvent> fromDocumentEvents(Document document, final Set<DocumentEvent.EventType> eventTypes) {
        return fromDocumentEvents(document).filter(new Func1<DocumentEvent, Boolean>() {
            @Override
            public Boolean call(DocumentEvent event) {
                return eventTypes.contains(event.getType());
            }
        });
    }

    /**
     * Creates an observable corresponding to JSlider changes.
     * 
     * @param component
     *            The slider to register the observable for.
     * @return Observable of change events.
     */
    public static Observable<ChangeEvent> fromChangeEvents(final JSlider component) {
        return create(new ChangeEventSource() {
            @Override
            protected void addListenerToComponent(ChangeListener listener) {
                component.addChangeListener(listener);
            }
            @Override
            protected void removeListenerFromComponent(ChangeListener listener) {
                component.removeChangeListener(listener);
            }
        });
    }

    /**
     * Creates an observable corresponding to JSpinner changes.
     * 
     * @param component
     *            The spinner to register the observable for.
     * @return Observable of change events.
     */
    public static Observable<ChangeEvent> fromChangeEvents(final JSpinner component) {
        return create(new ChangeEventSource() {
            @Override
            protected void addListenerToComponent(ChangeListener listener) {
                component.addChangeListener(listener);
            }
            @Override
            protected void removeListenerFromComponent(ChangeListener listener) {
                component.removeChangeListener(listener);
            }
        });
    }

    /**
     * Check if the current thead is the event dispatch thread.
     * 
     * @throws IllegalStateException if the current thread is not the event dispatch thread.
     */
    public static void assertEventDispatchThread() {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("Need to run in the event dispatch thread, but was " + Thread.currentThread());
        }
    }
}
