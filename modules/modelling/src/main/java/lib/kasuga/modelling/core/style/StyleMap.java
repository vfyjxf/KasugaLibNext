package lib.kasuga.modelling.core.style;

import lib.kasuga.modelling.core.element.Element;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public interface StyleMap {

    Set<StyleType<?>> keySet();

    boolean containsKey(StyleType<?> key);

    <T> T get(StyleType<T> key);

    int size();


    public static interface Writable extends StyleMap {
        <T> boolean put(StyleType<T> key, T value);

        void putAll(StyleMap otherMap);

        Set<StyleType<?>> apply(StyleMap otherMap, Element element);

        <T> T remove(StyleType<T> key);

        void clear();
    }

    public static class WritableHashMap implements Writable {
        Map<StyleType<?>, Object> styles;

        @Override
        public Set<StyleType<?>> keySet() {
            return styles.keySet();
        }

        @Override
        public boolean containsKey(StyleType<?> key) {
            return styles.containsKey(key);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T get(StyleType<T> key) {
            return (T) styles.get(key);
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public <T> boolean put(StyleType<T> key, T value) {
            styles.put(key, value);
            return true;
        }

        @Override
        public void putAll(StyleMap otherMap) {
            for (StyleType<?> key : otherMap.keySet()) {
                styles.put(key, otherMap.get(key));
            }
        }

        @Override
        public Set<StyleType<?>> apply(StyleMap otherMap, Element element) {
            HashSet<StyleType<?>> allKeys = new HashSet<>();
            allKeys.addAll(this.keySet());
            allKeys.addAll(otherMap.keySet());
            HashSet<StyleType<?>> changedKeys = new HashSet<>();
            for (StyleType<?> key : allKeys) {
                Object oldValue = this.styles.get(key);
                Object newValue = otherMap.get(key);
                if (oldValue != newValue) {
                    changedKeys.add(key);
                    //noinspection unchecked
                    ((StyleType<Object>)key).onApply(element, otherMap, newValue);
                    if (newValue != null) {
                        this.styles.put(key, newValue);
                    } else {
                        this.styles.remove(key);
                    }
                }
            }
            return changedKeys;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T remove(StyleType<T> key) {
            return (T) styles.remove(key);
        }

        @Override
        public void clear() {
            styles.clear();
        }
    }

    public class Empty implements StyleMap {
        @Override
        public Set<StyleType<?>> keySet() {
            return Set.of();
        }

        @Override
        public boolean containsKey(StyleType<?> key) {
            return false;
        }

        @Override
        public <T> T get(StyleType<T> key) {
            return null;
        }

        @Override
        public int size() {
            return 0;
        }
    }
}
