package nablarch.fw.dicontainer.component.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import nablarch.fw.dicontainer.component.ErrorCollector;
import nablarch.fw.dicontainer.exception.ContainerCreationException;
import nablarch.fw.dicontainer.exception.ContainerException;

/**
 * {@link ErrorCollector}実装クラス。
 */
public final class ErrorCollectorImpl implements ErrorCollector {

    /** 収集した例外 */
    private final List<ContainerException> exceptions = new ArrayList<>();

    /** 無視する例外クラス */
    private final Set<Class<? extends ContainerException>> ignoreExceptionClasses = new HashSet<>();

    @Override
    public void add(final ContainerException exception) {
        exceptions.add(exception);
    }

    @Override
    public void ignore(final Class<? extends ContainerException> ignoreMe) {
        ignoreExceptionClasses.add(ignoreMe);
    }

    @Override
    public void throwExceptionIfExistsError() {
        final List<ContainerException> filtered = exceptions.stream()
                .filter(a -> ignoreExceptionClasses.contains(a.getClass()) == false)
                .collect(Collectors.toList());
        if (filtered.isEmpty() == false) {
            throw new ContainerCreationException(filtered);
        }
    }
}
