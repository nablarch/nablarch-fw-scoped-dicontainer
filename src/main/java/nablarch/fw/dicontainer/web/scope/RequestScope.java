package nablarch.fw.dicontainer.web.scope;

import java.util.Objects;

import javax.inject.Provider;

import nablarch.fw.dicontainer.component.ComponentDefinition;
import nablarch.fw.dicontainer.component.ComponentId;
import nablarch.fw.dicontainer.exception.web.WebContextException;
import nablarch.fw.dicontainer.scope.AbstractScope;
import nablarch.fw.dicontainer.web.context.RequestContext;
import nablarch.fw.dicontainer.web.context.RequestContextSupplier;

/**
 * リクエストスコープ。
 *
 */
public final class RequestScope extends AbstractScope {

    /**
     * リクエストコンテキストを取得するクラス
     */
    private final RequestContextSupplier supplier;

    /**
     * インスタンスを生成する。
     * 
     * @param supplier リクエストコンテキストを取得するクラス
     */
    public RequestScope(final RequestContextSupplier supplier) {
        this.supplier = Objects.requireNonNull(supplier);
    }

    @Override
    public <T> T getComponent(final ComponentId id, final Provider<T> provider) {
        final RequestContext context = supplier.getRequestContext();
        if (context == null) {
            throw new WebContextException("RequestContext is not found.");
        }
        return context.getRequestComponent(id, provider);
    }

    @Override
    public int dimensions() {
        return 100;
    }

    /**
     * リクエストコンポーネントを破棄する。
     * 
     * @param id ID
     * @param component コンポーネント
     */
    public void destroyComponent(final ComponentId id, final Object component) {
        final ComponentDefinition<Object> definition = (ComponentDefinition<Object>) idToDefinition
                .get(id);
        definition.destroyComponent(component);
    }
}
