package nablarch.fw.dicontainer.component;

import nablarch.fw.dicontainer.container.ContainerBuilder;
import nablarch.fw.dicontainer.container.ContainerImplementer;
import nablarch.fw.dicontainer.container.CycleDependencyValidationContext;

/**
 * インジェクションされるコンストラクタ・プロバイダを表すインターフェース。
 *
 */
public interface InjectableConstructor {

    /**
     * インジェクションを行う。
     * 
     * @param container DIコンテナ
     * @return コンストラクタ・メソッドの場合は戻り値が返される。フィールドの場合は{@literal null}が返される。
     */
    Object inject(ContainerImplementer container);

    /**
     * バリデーションを行う。
     * 
     * @param containerBuilder DIコンテナのビルダー
     * @param self 自身を含んでいるコンポーネント定義
     */
    void validate(ContainerBuilder<?> containerBuilder, ComponentDefinition<?> self);

    /**
     * 依存関係の循環を検出するためのバリデーションを行う。
     * 
     * @param context 循環依存バリデーションのコンテキスト
     */
    void validateCycleDependency(CycleDependencyValidationContext context);
}