package au.akanedev.simplemimics.api.events;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class SimpleEvent<T> {

    private final CopyOnWriteArrayList<T> listeners =
            new CopyOnWriteArrayList<>();

    public void register(T listener) {
        listeners.add(listener);
    }

    public void unregister(T listener) {
        listeners.remove(listener);
    }

    public void invoke(Consumer<T> invoker) {
        for (T listener : listeners) {
            invoker.accept(listener);
        }
    }

    public void clear() {
        listeners.clear();
    }

    public int size() {
        return listeners.size();
    }
}