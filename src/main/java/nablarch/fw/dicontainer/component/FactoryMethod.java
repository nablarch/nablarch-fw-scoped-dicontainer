package nablarch.fw.dicontainer.component;

import java.util.Objects;

import nablarch.fw.dicontainer.ComponentKey;
import nablarch.fw.dicontainer.container.ContainerBuilder;

public interface FactoryMethod {

    void apply(ContainerBuilder<?> containerBuilder);

    class FactoryMethodImpl implements FactoryMethod {

        private final ComponentKey<?> key;
        private final ComponentDefinition<?> definition;

        public FactoryMethodImpl(final ComponentKey<?> key,
                final ComponentDefinition<?> definition) {
            this.key = Objects.requireNonNull(key);
            this.definition = Objects.requireNonNull(definition);
        }

        @Override
        public void apply(final ContainerBuilder<?> containerBuilder) {
            containerBuilder.register((ComponentKey) key, (ComponentDefinition) definition);
        }
    }
}