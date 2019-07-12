package nablarch.fw.dicontainer;

import java.util.Set;

import javax.inject.Provider;

public final class PrototypeScope implements Scope {

    @Override
    public <T> T getComponent(final ComponentKey<T> key, final Provider<T> provider,
            final Set<DestroyMethod> destroyMethods) {
        return provider.get();
    }
}
