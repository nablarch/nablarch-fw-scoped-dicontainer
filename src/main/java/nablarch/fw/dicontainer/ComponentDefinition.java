package nablarch.fw.dicontainer;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import javax.inject.Provider;

public final class ComponentDefinition<T> {

    private final InjectableMember injectableConstructor;
    private final Set<InjectableMember> injectableMembers;
    private final Set<ObservesMethod> observesMethods;
    private final InitMethod initMethod;
    private final DestroyMethod destroyMethod;
    private final Scope scope;

    public ComponentDefinition(final InjectableMember injectableConstructor,
            final Set<InjectableMember> injectableMembers,
            final Set<ObservesMethod> observesMethods,
            final InitMethod initMethod,
            final DestroyMethod destroyMethod,
            final Scope scope) {
        this.injectableConstructor = Objects.requireNonNull(injectableConstructor);
        this.injectableMembers = Objects.requireNonNull(injectableMembers);
        this.observesMethods = Objects.requireNonNull(observesMethods);
        this.initMethod = Objects.requireNonNull(initMethod);
        this.destroyMethod = Objects.requireNonNull(destroyMethod);
        this.scope = Objects.requireNonNull(scope);
    }

    public static <T> Builder<T> builderFromAnnotation(final Class<T> componentType) {
        final InjectableConstructor<T> injectableConstructor = InjectableConstructor
                .fromAnnotation(componentType);
        final Set<InjectableMember> injectableMembers = InjectableMember
                .fromAnnotation(componentType);
        final Set<ObservesMethod> observesMethods = ObservesMethod
                .fromAnnotation(componentType);
        final InitMethod initMethod = InitMethod.fromAnnotation(componentType);
        final DestroyMethod destroyMethod = DestroyMethod.fromAnnotation(componentType);
        final Builder<T> builder = builder();
        return builder
                .injectableConstructor(injectableConstructor)
                .injectableMembers(injectableMembers)
                .observesMethods(observesMethods)
                .initMethod(initMethod)
                .destroyMethod(destroyMethod);
    }

    public T getComponent(final Container container, final ComponentKey<T> key) {
        final Provider<T> provider = new Provider<T>() {
            @Override
            public T get() {
                final Object component = injectableConstructor.inject(container, null);
                for (final InjectableMember injectableMember : injectableMembers) {
                    injectableMember.inject(container, component);
                }
                initMethod.invoke(component);
                return (T) component;
            }
        };
        return scope.getComponent(key, provider, destroyMethod);
    }

    public void fire(final Container container, final ComponentKey<T> key, final Object event) {
        for (final ObservesMethod observesMethod : observesMethods) {
            if (observesMethod.isTarget(event)) {
                final T component = getComponent(container, key);
                observesMethod.invoke(component, event);
            }
        }
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static final class Builder<T> {

        private InjectableMember injectableConstructor;
        private Set<InjectableMember> injectableMembers = Collections.emptySet();
        private Set<ObservesMethod> observesMethods = Collections.emptySet();
        private InitMethod initMethod = InitMethod.noop();
        private DestroyMethod destroyMethod = DestroyMethod.noop();
        private Scope scope;

        public Builder<T> injectableConstructor(final InjectableMember injectableConstructor) {
            this.injectableConstructor = injectableConstructor;
            return this;
        }

        public Builder<T> injectableMembers(final Set<InjectableMember> injectableMembers) {
            this.injectableMembers = injectableMembers;
            return this;
        }

        public Builder<T> observesMethods(final Set<ObservesMethod> observesMethods) {
            this.observesMethods = observesMethods;
            return this;
        }

        public Builder<T> initMethod(final InitMethod initMethod) {
            this.initMethod = initMethod;
            return this;
        }

        public Builder<T> destroyMethod(final DestroyMethod destroyMethod) {
            this.destroyMethod = destroyMethod;
            return this;
        }

        public Builder<T> scope(final Scope scope) {
            this.scope = scope;
            return this;
        }

        public ComponentDefinition<T> build() {
            return new ComponentDefinition<>(injectableConstructor, injectableMembers,
                    observesMethods, initMethod, destroyMethod, scope);
        }
    }
}
