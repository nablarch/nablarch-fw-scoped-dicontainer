package nablarch.fw.dicontainer;

import java.lang.annotation.Annotation;
import java.util.Objects;

import javax.inject.Named;

public class NamedImpl implements Named {

    private final String value;

    public NamedImpl(final String value) {
        this.value = Objects.requireNonNull(value);
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return Named.class;
    }

    @Override
    public String value() {
        return value;
    }

    @Override
    public int hashCode() {
        return (127 * "value".hashCode()) ^ value.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof Named == false) {
            return false;
        }
        final Named other = (Named) obj;
        return value.equals(other.value());
    }
}
