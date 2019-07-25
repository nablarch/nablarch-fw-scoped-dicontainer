package nablarch.fw.dicontainer.scope;

import java.lang.reflect.Method;
import java.util.Optional;

import nablarch.fw.dicontainer.component.ErrorCollector;
import nablarch.fw.dicontainer.component.factory.MemberFactory;
import nablarch.fw.dicontainer.container.ContainerBuilder;

/**
 * スコープを決定するクラス。
 *
 */
public interface ScopeDecider {

    /**
     * コンポーネントのクラスが持つアノテーションからスコープを決定する。
     * 
     * @param componentType コンポーネントのクラス
     * @param errorCollector バリデーションエラーを収集するクラス
     * @return スコープ
     */
    Optional<Scope> fromComponentClass(Class<?> componentType,
            ErrorCollector errorCollector);

    /**
     * ファクトリメソッドが持つアノテーションからスコープを決定する。
     * 
     * @param factoryMethod ファクトリメソッド
     * @param errorCollector バリデーションエラーを収集するクラス
     * @return スコープ
     */
    Optional<Scope> fromFactoryMethod(Method factoryMethod,
            ErrorCollector errorCollector);

    /**
     * スコープをコンポーネント登録する。
     * 
     * @param builder DIコンテナのビルダー
     * @param memberFactory TODO
     */
    void registerScopes(ContainerBuilder<?> builder,
            MemberFactory memberFactory);

}