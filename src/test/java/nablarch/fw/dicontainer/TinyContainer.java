package nablarch.fw.dicontainer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public final class TinyContainer implements Container {

    private final Map<ComponentKey<?>, Object> components = new HashMap<>();

    public TinyContainer(final Class<?>... componentTypes) throws Exception {
        for (final Class<?> componentType : componentTypes) {
            register(componentType);
        }
    }

    public TinyContainer register(final Class<?> componentType) throws Exception {
        final ComponentKey<?> key = ComponentKey.fromClass(componentType);
        final Constructor<?> constructor = componentType.getDeclaredConstructor();
        if (constructor.isAccessible() == false) {
            constructor.setAccessible(true);
        }
        final Object value = constructor.newInstance();
        return register(key, value);
    }

    public TinyContainer register(final ComponentKey<?> key, final Object value) throws Exception {
        components.put(key, value);
        return this;
    }

    @Override
    public <T> T getComponent(final ComponentKey<T> key) {
        return (T) components.get(key);
    }

    @Override
    public <T> T getComponent(final Class<T> key) {
        return getComponent(ComponentKey.fromClass(key));
    }

    @Override
    public <T> T getComponent(final Class<T> key, final Annotation... qualifiers) {
        return getComponent(new ComponentKey<>(key, Arrays.stream(qualifiers)
                .map(Qualifier::fromAnnotation).collect(Collectors.toSet())));
    }

    @Override
    public void fire(final Object event) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void destroy() {
        throw new UnsupportedOperationException();
    }
}