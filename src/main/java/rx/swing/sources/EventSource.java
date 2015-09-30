package rx.swing.sources;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

public abstract class EventSource<Event, Listener> implements Observable.OnSubscribe<Event> {

    protected abstract Listener createListenerFor(Subscriber<? super Event> subscriber);

    protected abstract void addListenerToComponent(Listener listener);

    protected abstract void removeListenerFromComponent(Listener listener);

    @Override
    public void call(final Subscriber<? super Event> subscriber) {
        final Listener listener = createListenerFor(subscriber);
        addListenerToComponent(listener);
        subscriber.add(Subscriptions.create(new Action0() {
            @Override
            public void call() {
                removeListenerFromComponent(listener);
            }
        }));
    }

}
