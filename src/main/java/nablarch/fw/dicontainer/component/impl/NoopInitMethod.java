package nablarch.fw.dicontainer.component.impl;

import nablarch.fw.dicontainer.component.ComponentDefinition;
import nablarch.fw.dicontainer.component.InitMethod;
import nablarch.fw.dicontainer.container.ContainerBuilder;

/**
 * 何もしない{@link InitMethod}実装クラス。
 */
public final class NoopInitMethod implements InitMethod {

    @Override
    public void invoke(final Object component) {
    }

    @Override
    public void validate(final ContainerBuilder<?> containerBuilder,
            final ComponentDefinition<?> self) {
    }
}
