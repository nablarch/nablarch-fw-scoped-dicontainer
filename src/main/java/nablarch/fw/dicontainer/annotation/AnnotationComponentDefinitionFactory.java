package nablarch.fw.dicontainer.annotation;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import nablarch.fw.dicontainer.ComponentId;
import nablarch.fw.dicontainer.config.ComponentDefinition;
import nablarch.fw.dicontainer.config.ComponentDefinition.Builder;
import nablarch.fw.dicontainer.config.DestroyMethod;
import nablarch.fw.dicontainer.config.ErrorCollector;
import nablarch.fw.dicontainer.config.FactoryMethod;
import nablarch.fw.dicontainer.config.InitMethod;
import nablarch.fw.dicontainer.config.InjectableMember;
import nablarch.fw.dicontainer.config.ObservesMethod;
import nablarch.fw.dicontainer.config.Scope;

public final class AnnotationComponentDefinitionFactory {

    private final AnnotationMemberFactory memberFactory;
    private final AnnotationScopeDecider scopeDecider;

    public AnnotationComponentDefinitionFactory(final AnnotationMemberFactory memberFactory,
            final AnnotationScopeDecider scopeDecider) {
        this.memberFactory = Objects.requireNonNull(memberFactory);
        this.scopeDecider = Objects.requireNonNull(scopeDecider);
    }

    public <T> Optional<ComponentDefinition<T>> fromClass(final Class<T> componentType,
            final ErrorCollector errorCollector) {
        final Builder<T> builder = ComponentDefinition.builder();

        final Optional<InjectableMember> injectableConstructor = memberFactory
                .createConstructor(componentType, errorCollector);
        final Set<InjectableMember> injectableMembers = memberFactory
                .createFieldsAndMethods(componentType, errorCollector);
        final Set<ObservesMethod> observesMethods = memberFactory
                .createObservesMethod(componentType, errorCollector);
        final Optional<InitMethod> initMethod = memberFactory.createInitMethod(componentType,
                errorCollector);
        final Optional<DestroyMethod> destroyMethod = memberFactory
                .createDestroyMethod(componentType, errorCollector);
        final Set<FactoryMethod> factoryMethods = memberFactory.createFactoryMethods(builder.id(),
                componentType, this, errorCollector);
        final Optional<Scope> scope = scopeDecider.fromClass(componentType, errorCollector);

        injectableConstructor.ifPresent(builder::injectableConstructor);
        initMethod.ifPresent(builder::initMethod);
        destroyMethod.ifPresent(builder::destroyMethod);
        scope.ifPresent(builder::scope);

        return builder
                .injectableMembers(injectableMembers)
                .observesMethods(observesMethods)
                .factoryMethods(factoryMethods)
                .build();
    }

    public <T> Optional<ComponentDefinition<T>> fromMethod(final ComponentId factoryId,
            final Method method,
            final ErrorCollector errorCollector) {
        final Builder<T> builder = ComponentDefinition.builder();

        final InjectableMember injectableConstructor = memberFactory.createFactoryMethod(factoryId,
                method, errorCollector);
        final Optional<DestroyMethod> destroyMethod = memberFactory.createFactoryDestroyMethod(
                method,
                errorCollector);
        final Optional<Scope> scope = scopeDecider.fromMethod(method, errorCollector);

        destroyMethod.ifPresent(builder::destroyMethod);
        scope.ifPresent(builder::scope);

        return builder
                .injectableConstructor(injectableConstructor)
                .build();
    }
}