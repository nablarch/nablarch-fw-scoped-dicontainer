package nablarch.fw.dicontainer.annotation;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Provider;
import javax.inject.Singleton;

import nablarch.fw.dicontainer.Prototype;
import nablarch.fw.dicontainer.component.ComponentDefinition;
import nablarch.fw.dicontainer.component.ComponentId;
import nablarch.fw.dicontainer.component.ComponentKey;
import nablarch.fw.dicontainer.component.DestroyMethod;
import nablarch.fw.dicontainer.component.ErrorCollector;
import nablarch.fw.dicontainer.component.InjectableConstructor;
import nablarch.fw.dicontainer.component.InjectableMember;
import nablarch.fw.dicontainer.component.ObservesMethod;
import nablarch.fw.dicontainer.component.factory.MemberFactory;
import nablarch.fw.dicontainer.component.impl.PassthroughInjectableConstructor;
import nablarch.fw.dicontainer.container.ContainerBuilder;
import nablarch.fw.dicontainer.exception.ScopeDuplicatedException;
import nablarch.fw.dicontainer.exception.ScopeNotFoundException;
import nablarch.fw.dicontainer.scope.PrototypeScope;
import nablarch.fw.dicontainer.scope.Scope;
import nablarch.fw.dicontainer.scope.ScopeDecider;
import nablarch.fw.dicontainer.scope.SingletonScope;

/**
 * アノテーションをもとにしてスコープを決定するクラス。
 *
 */
public final class AnnotationScopeDecider implements ScopeDecider {

    /**
     * デフォルトのスコープ
     */
    private final Scope defaultScope;
    /**
     * アノテーションとスコープのマッピング
     */
    private final Map<Class<? extends Annotation>, Scope> scopes;
    /**
     * スコープをコンポーネント登録する際に使用されるスコープ
     */
    private final Scope passthroughScope = new ScopeScope();

    /**
     * インスタンスを生成する。
     * 
     * @param defaultScope デフォルトのスコープ
     * @param scopes アノテーションとスコープのマッピング
     */
    private AnnotationScopeDecider(final Scope defaultScope,
            final Map<Class<? extends Annotation>, Scope> scopes) {
        this.defaultScope = Objects.requireNonNull(defaultScope);
        this.scopes = Objects.requireNonNull(scopes);
    }

    @Override
    public Optional<Scope> fromComponentClass(final Class<?> componentType,
            final ErrorCollector errorCollector) {
        final Source source = new ComponentClassSource(componentType);
        return source.decide(errorCollector);
    }

    @Override
    public void registerScopes(final ContainerBuilder<?> builder,
            final MemberFactory memberFactory) {
        for (final Scope scope : scopes.values()) {
            registerScope(builder, memberFactory, scope);
        }
        if (scopes.values().stream().anyMatch(a -> a == defaultScope) == false) {
            registerScope(builder, memberFactory, defaultScope);
        }
    }

    /**
     * スコープをコンポーネント登録する。
     * 
     * @param builder DIコンテナのビルダー
     * @param memberFactory {@link MemberFactory}実装
     * @param scope スコープ
     */
    private <T extends Scope> void registerScope(final ContainerBuilder<?> builder,
            final MemberFactory memberFactory, final T scope) {

        final ErrorCollector errorCollector = ErrorCollector.wrap(builder);

        final Class<T> componentType = (Class<T>) scope.getClass();
        final ComponentKey<T> key = new ComponentKey<>(componentType);
        final InjectableConstructor injectableConstructor = new PassthroughInjectableConstructor(
                scope);
        final List<InjectableMember> injectableMembers = memberFactory
                .createFieldsAndMethods(componentType, errorCollector);
        final List<ObservesMethod> observesMethods = memberFactory.createObservesMethod(
                componentType, errorCollector);
        final Optional<DestroyMethod> destroyMethod = memberFactory.createDestroyMethod(
                componentType, errorCollector);

        final ComponentDefinition.Builder<T> cdBuilder = ComponentDefinition.builder(componentType);
        destroyMethod.ifPresent(cdBuilder::destroyMethod);

        final Optional<ComponentDefinition<T>> definition = cdBuilder
                .injectableConstructor(injectableConstructor)
                .injectableMembers(injectableMembers)
                .observesMethods(observesMethods)
                .scope(passthroughScope)
                .build();

        definition.ifPresent(a -> builder.register(key, a));
    }

    /**
     * デフォルトの設定をしたインスタンスを構築する。
     * 
     * @return インスタンス
     */
    public static AnnotationScopeDecider createDefault() {
        return builder().build();
    }

    /**
     * ビルダーを生成する。
     * 
     * @return ビルダー
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 状態をコピーしてビルダーを生成する。
     * 
     * @param source コピー元
     * @return ビルダー
     */
    public static Builder builderFrom(final AnnotationScopeDecider source) {
        final Builder builder = builder();
        builder.defaultScope = source.defaultScope;
        builder.scopes.putAll(source.scopes);
        return builder;
    }

    /**
     * ビルダー。
     *
     */
    public static final class Builder {

        /**
         * デフォルトのスコープ
         */
        private Scope defaultScope;
        /**
         * アノテーションとスコープのマッピング
         */
        private final Map<Class<? extends Annotation>, Scope> scopes = new HashMap<>();

        /**
         * インスタンスを生成する。
         */
        private Builder() {
            final Scope prototypeScope = new PrototypeScope();
            this.scopes.put(Prototype.class, prototypeScope);
            this.scopes.put(Singleton.class, new SingletonScope());
            this.defaultScope = prototypeScope;
        }

        /**
         * デフォルトのスコープを設定する。
         * 
         * @param defaultScope デフォルトのスコープ
         * @return このビルダー自身
         */
        public Builder defaultScope(final Scope defaultScope) {
            this.defaultScope = defaultScope;
            return this;
        }

        /**
         * アノテーションとスコープのマッピングを追加する。
         * 
         * @param annotationType アノテーションの型
         * @param scope スコープ
         * @return このビルダー自身
         */
        public Builder addScope(final Class<? extends Annotation> annotationType,
                final Scope scope) {
            this.scopes.put(annotationType, scope);
            return this;
        }

        /**
         * シングルトンのコンポーネントをイーガーロードするかどうかを設定する。
         * 
         * @param eagerLoad イーガーロードする場合は{@literal true}
         * @return このビルダー自身
         */
        public Builder eagerLoad(final boolean eagerLoad) {
            this.scopes.put(Singleton.class, new SingletonScope(eagerLoad));
            return this;
        }

        /**
         * インスタンスを構築する。
         * 
         * @return 構築されたインスタンス
         */
        public AnnotationScopeDecider build() {
            return new AnnotationScopeDecider(defaultScope, scopes);
        }
    }

    private abstract class Source {

        protected abstract Annotation[] getAnnotations();

        protected abstract ScopeDuplicatedException newScopeDuplicatedException();

        protected abstract ScopeNotFoundException newScopeNotFoundException(
                Class<? extends Annotation> annotation);

        private Optional<Scope> decide(final ErrorCollector errorCollector) {
            final Set<Annotation> annotations = Arrays.stream(getAnnotations())
                    .filter(a -> a.annotationType().isAnnotationPresent(javax.inject.Scope.class))
                    .collect(Collectors.toSet());
            if (annotations.isEmpty()) {
                return Optional.of(defaultScope);
            } else if (annotations.size() > 1) {
                errorCollector.add(newScopeDuplicatedException());
                return Optional.empty();
            }
            final Class<? extends Annotation> annotation = annotations.iterator().next()
                    .annotationType();
            final Scope scope = scopes.get(annotation);
            if (scope == null) {
                errorCollector.add(newScopeNotFoundException(annotation));
                return Optional.empty();
            }
            return Optional.of(scope);
        }
    }

    private final class ComponentClassSource extends Source {

        private final Class<?> componentType;

        public ComponentClassSource(final Class<?> componentType) {
            this.componentType = Objects.requireNonNull(componentType);
        }

        @Override
        protected Annotation[] getAnnotations() {
            return componentType.getAnnotations();
        }

        @Override
        protected ScopeDuplicatedException newScopeDuplicatedException() {
            return new ScopeDuplicatedException(
                    "Component [" + componentType.getName() + "] can configured only one scope.");
        }

        @Override
        protected ScopeNotFoundException newScopeNotFoundException(
                final Class<? extends Annotation> annotation) {
            return new ScopeNotFoundException(
                    "Scope could not be decided from ["
                            + annotation.getName() + "] annotated to [" + componentType.getName()
                            + "]");
        }
    }

    /**
     * スコープをコンポーネント登録する際に使用するためのスコープ。
     *
     */
    private static final class ScopeScope implements Scope {

        @Override
        public <T> T getComponent(final ComponentId id, final Provider<T> provider) {
            return provider.get();
        }

        @Override
        public int dimensions() {
            return Integer.MAX_VALUE;
        }

        @Override
        public <T> void register(final ComponentDefinition<T> definition) {
        }
    }
}
