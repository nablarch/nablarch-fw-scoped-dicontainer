package nablarch.fw.dicontainer.config;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.inject.Provider;

import nablarch.fw.dicontainer.ComponentId;
import nablarch.fw.dicontainer.Observes;
import nablarch.fw.dicontainer.Scope;

public final class SingletonScope implements Scope {

    private final ConcurrentMap<ComponentId, InstanceHolder> instances = new ConcurrentHashMap<>();

    @Override
    public <T> T getComponent(final ComponentId id, final Provider<T> provider,
            final DestroyMethod destroyMethod) {
        InstanceHolder instanceHolder = instances.get(id);
        if (instanceHolder == null) {
            instanceHolder = new InstanceHolder(destroyMethod);
            final InstanceHolder previous = instances.putIfAbsent(id, instanceHolder);
            if (previous != null && instanceHolder != previous) {
                instanceHolder = previous;
            }
        }
        return instanceHolder.get(provider);
    }

    @Observes
    public void destroy(final ContainerDestroy event) {
        for (final InstanceHolder instance : instances.values()) {
            instance.destroy();
        }
    }

    @Override
    public int dimensions() {
        return Integer.MAX_VALUE;
    }

    private static class InstanceHolder {

        Object instance;
        final Lock lock = new ReentrantLock();
        private final DestroyMethod destroyMethod;

        InstanceHolder(final DestroyMethod destroyMethod) {
            this.destroyMethod = Objects.requireNonNull(destroyMethod);
        }

        void destroy() {
            lock.lock();
            try {
                if (instance != null) {
                    destroyMethod.invoke(instance);
                }
            } finally {
                lock.unlock();
            }
        }

        <T> T get(final Provider<T> provider) {
            lock.lock();
            try {
                if (instance == null) {
                    instance = provider.get();
                }
                return (T) instance;
            } finally {
                lock.unlock();
            }
        }
    }
}
