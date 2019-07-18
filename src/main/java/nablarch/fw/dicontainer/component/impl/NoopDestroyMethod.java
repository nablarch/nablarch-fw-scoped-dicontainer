package nablarch.fw.dicontainer.component.impl;

import nablarch.fw.dicontainer.component.DestroyMethod;

public final class NoopDestroyMethod implements DestroyMethod {

    @Override
    public void invoke(final Object component) {
    }
}
