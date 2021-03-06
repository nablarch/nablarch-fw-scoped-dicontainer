package nablarch.fw.dicontainer.component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.inject.Provider;

import nablarch.fw.dicontainer.Container;
import nablarch.fw.dicontainer.component.impl.NoopDestroyMethod;
import nablarch.fw.dicontainer.component.impl.NoopInitMethod;
import nablarch.fw.dicontainer.container.ContainerBuilder;
import nablarch.fw.dicontainer.container.CycleDependencyValidationContext;
import nablarch.fw.dicontainer.scope.ComponentRemoveableScope;
import nablarch.fw.dicontainer.scope.Scope;

/**
 * コンポーネント定義
 *
 * @param <T> コンポーネントの型
 */
public final class ComponentDefinition<T> {

    /**
     * ID
     */
    private final ComponentId id;
    /**
     * コンポーネントのクラス
     */
    private final Class<T> componentType;
    /**
     * コンポーネントを生成するコンストラクタ・プロバイダ
     */
    private final InjectableConstructor injectableConstructor;
    /**
     * インジェクションされるメソッド・フィールド
     */
    private final List<InjectableMember> injectableMembers;
    /**
     * イベントハンドラメソッド
     */
    private final List<ObservesMethod> observesMethods;
    /**
     * 初期化メソッド
     */
    private final InitMethod initMethod;
    /**
     * 破棄メソッド
     */
    private final DestroyMethod destroyMethod;
    /**
     * スコープ
     */
    private final Scope scope;

    /**
     * インスタンスを生成する。
     * 
     * @param id ID
     * @param componentType コンポーネントのクラス
     * @param injectableConstructor コンポーネントを生成するコンストラクタ・メソッド
     * @param injectableMembers インジェクションされるメソッド・フィールド
     * @param observesMethods イベントハンドラメソッド
     * @param initMethod 初期化メソッド
     * @param destroyMethod 破棄メソッド
     * @param scope スコープ
     */
    private ComponentDefinition(final ComponentId id,
            final Class<T> componentType,
            final InjectableConstructor injectableConstructor,
            final List<InjectableMember> injectableMembers,
            final List<ObservesMethod> observesMethods,
            final InitMethod initMethod,
            final DestroyMethod destroyMethod,
            final Scope scope) {
        this.id = Objects.requireNonNull(id);
        this.componentType = Objects.requireNonNull(componentType);
        this.injectableConstructor = Objects.requireNonNull(injectableConstructor);
        this.injectableMembers = Objects.requireNonNull(injectableMembers);
        this.observesMethods = Objects.requireNonNull(observesMethods);
        this.initMethod = Objects.requireNonNull(initMethod);
        this.destroyMethod = Objects.requireNonNull(destroyMethod);
        this.scope = Objects.requireNonNull(scope);

        this.scope.register(this);
    }

    /**
     * IDを取得する。
     * 
     * @return ID
     */
    public ComponentId getId() {
        return id;
    }

    /**
     * バリデーションを行う。
     * 
     * @param containerBuilder DIコンテナのビルダー
     */
    public void validate(final ContainerBuilder<?> containerBuilder) {
        injectableConstructor.validate(containerBuilder, this);
        for (final InjectableMember injectableMember : injectableMembers) {
            injectableMember.validate(containerBuilder, this);
        }
        for (final ObservesMethod observesMethod : observesMethods) {
            observesMethod.validate(containerBuilder, this);
        }
        initMethod.validate(containerBuilder, this);
        destroyMethod.validate(containerBuilder, this);
    }

    /**
     * 渡されたコンポーネント定義よりもスコープが狭いかどうかを返す。
     * 
     * @param injected インジェクションされるコンポーネントの定義
     * @return 自身の方がスコープが狭い場合は{@literal true}を返す
     */
    public boolean isNarrowScope(final ComponentDefinition<?> injected) {
        return scope.dimensions() <= injected.scope.dimensions();
    }

    /**
     * 依存関係の循環を検出するためのバリデーションを行う。
     * 
     * @param context 循環依存バリデーションのコンテキスト
     */
    public void validateCycleDependency(final CycleDependencyValidationContext context) {
        injectableConstructor.validateCycleDependency(context.createSubContext());
        for (final InjectableMember injectableMember : injectableMembers) {
            injectableMember.validateCycleDependency(context.createSubContext());
        }
    }

    /**
     * コンポーネントを取得する。
     * 
     * @param container DIコンテナ
     * @return コンポーネント
     */
    public T getComponent(final Container container) {
        Objects.requireNonNull(container);
        final Provider<T> provider = new Provider<T>() {
            @Override
            public T get() {
                final Object component = injectableConstructor.inject(container);
                for (final InjectableMember injectableMember : injectableMembers) {
                    injectableMember.inject(container, component);
                }
                initMethod.invoke(component);
                return componentType.cast(component);
            }
        };
        return scope.getComponent(id, provider);
    }

    /**
     * コンポーネントを削除する。
     *
     * コンポーネントの削除は{@link ComponentRemoveableScope}を実装したスコープのみ使用できる。
     *
     * @return コンポーネント
     */
    public T removeComponent() {
        if (!(scope instanceof ComponentRemoveableScope)) {
            throw new UnsupportedOperationException("Removing of component is supported in scope that implements ComponentRemoveableScope.");
        }
        return ((ComponentRemoveableScope) scope).removeComponent(id);
    }

    /**
     * イベントを発火させる。
     * 
     * @param container DIコンテナ
     * @param event イベント
     */
    public void fire(final Container container, final Object event) {
        for (final ObservesMethod observesMethod : observesMethods) {
            if (observesMethod.isTarget(event)) {
                final T component = getComponent(container);
                observesMethod.invoke(component, event);
            }
        }
    }

    /**
     * コンポーネントを破棄する。
     * 
     * @param component コンポーネント
     */
    public void destroyComponent(final T component) {
        destroyMethod.invoke(component);
    }

    @Override
    public String toString() {
        return "Component(class=" + componentType.getName() + ", scope="
                + scope.getClass().getSimpleName() + ")";
    }

    /**
     * ビルダーのインスタンスを生成する。
     * 
     * @param <T> コンポーネントの型
     * @param componentType コンポーネントのクラス
     * @return ビルダー
     */
    public static <T> Builder<T> builder(final Class<T> componentType) {
        return new Builder<>(componentType);
    }

    /**
     * コンポーネント定義のビルダー
     *
     * @param <T> コンポーネントの型
     */
    public static final class Builder<T> {

        /**
         * ID
         */
        private final ComponentId id = ComponentId.generate();
        /**
         * コンポーネントのクラス
         */
        private final Class<T> componentType;
        /**
         * コンポーネントを生成するコンストラクタ・メソッド
         */
        private InjectableConstructor injectableConstructor;
        /**
         * インジェクションされるメソッド・フィールド
         */
        private List<InjectableMember> injectableMembers = Collections.emptyList();
        /**
         * イベントハンドラメソッド
         */
        private List<ObservesMethod> observesMethods = Collections.emptyList();
        /**
         * 初期化メソッド
         */
        private InitMethod initMethod = new NoopInitMethod();
        /**
         * 破棄メソッド
         */
        private DestroyMethod destroyMethod = new NoopDestroyMethod();
        /**
         * スコープ
         */
        private Scope scope;

        private Builder(final Class<T> componentType) {
            this.componentType = Objects.requireNonNull(componentType);
        }

        /**
         * IDを返す。
         * 
         * @return ID
         */
        public ComponentId id() {
            return id;
        }

        /**
         * コンポーネントを生成するコンストラクタ・メソッドを設定する。
         * 
         * @param injectableConstructor コンポーネントを生成するコンストラクタ・メソッド
         * @return このビルダー自身
         */
        public Builder<T> injectableConstructor(
                final InjectableConstructor injectableConstructor) {
            this.injectableConstructor = injectableConstructor;
            return this;
        }

        /**
         * インジェクションされるメソッド・フィールドを設定する。
         * 
         * @param injectableMembers インジェクションされるメソッド・フィールド
         * @return このビルダー自身
         */
        public Builder<T> injectableMembers(final List<InjectableMember> injectableMembers) {
            this.injectableMembers = injectableMembers;
            return this;
        }

        /**
         * イベントハンドラメソッドを設定する。
         * 
         * @param observesMethods イベントハンドラメソッド
         * @return このビルダー自身
         */
        public Builder<T> observesMethods(final List<ObservesMethod> observesMethods) {
            this.observesMethods = observesMethods;
            return this;
        }

        /**
         * 初期化メソッドを設定する。
         * 
         * @param initMethod 初期化メソッド
         * @return このビルダー自身
         */
        public Builder<T> initMethod(final InitMethod initMethod) {
            this.initMethod = initMethod;
            return this;
        }

        /**
         * 破棄メソッドを設定する。
         * 
         * @param destroyMethod 破棄メソッド
         * @return このビルダー自身
         */
        public Builder<T> destroyMethod(final DestroyMethod destroyMethod) {
            this.destroyMethod = destroyMethod;
            return this;
        }

        /**
         * スコープを設定する。
         * 
         * @param scope スコープ
         * @return このビルダー自身
         */
        public Builder<T> scope(final Scope scope) {
            this.scope = scope;
            return this;
        }

        /**
         * コンポーネント定義を構築する。
         * 
         * @return コンポーネント定義
         */
        public Optional<ComponentDefinition<T>> build() {
            if (injectableConstructor == null) {
                return Optional.empty();
            }
            if (scope == null) {
                return Optional.empty();
            }
            final ComponentDefinition<T> cd = new ComponentDefinition<>(id, componentType,
                    injectableConstructor, injectableMembers, observesMethods, initMethod,
                    destroyMethod, scope);
            return Optional.of(cd);
        }
    }
}
