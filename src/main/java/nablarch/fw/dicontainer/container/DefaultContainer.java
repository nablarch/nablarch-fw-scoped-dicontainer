package nablarch.fw.dicontainer.container;

import java.lang.annotation.Annotation;
import java.util.Objects;
import java.util.Set;

import nablarch.fw.dicontainer.component.AliasMapping;
import nablarch.fw.dicontainer.component.ComponentDefinition;
import nablarch.fw.dicontainer.component.ComponentDefinitionRepository;
import nablarch.fw.dicontainer.component.ComponentId;
import nablarch.fw.dicontainer.component.ComponentKey;
import nablarch.fw.dicontainer.event.ContainerDestroy;
import nablarch.fw.dicontainer.exception.ComponentDuplicatedException;
import nablarch.fw.dicontainer.exception.ComponentNotFoundException;

public final class DefaultContainer implements ContainerImplementer {

    private final ComponentDefinitionRepository definitions;
    private final AliasMapping aliasMapping;

    public DefaultContainer(final ComponentDefinitionRepository definitionsMap,
            final AliasMapping aliasesMap) {
        this.definitions = Objects.requireNonNull(definitionsMap);
        this.aliasMapping = Objects.requireNonNull(aliasesMap);
    }

    @Override
    public <T> T getComponent(final ComponentId id) {
        final ComponentDefinition<T> definition = definitions.get(id);
        return definition.getComponent(this);
    }

    @Override
    public <T> ComponentDefinition<T> getComponentDefinition(final ComponentId id) {
        return definitions.get(id);
    }

    @Override
    public <T> T getComponent(final ComponentKey<T> key) {
        ComponentDefinition<T> definition = definitions.find(key);
        if (definition != null) {
            return definition.getComponent(this);
        }
        final Set<ComponentKey<?>> alterKeys = aliasMapping.find(key.asAliasKey());
        if (alterKeys.isEmpty()) {
            throw new ComponentNotFoundException();
        } else if (alterKeys.size() > 1) {
            throw new ComponentDuplicatedException();
        }
        final ComponentKey<T> alterKey = (ComponentKey<T>) alterKeys.iterator().next();
        definition = definitions.find(alterKey);
        return definition.getComponent(this);
    }

    @Override
    public <T> T getComponent(final Class<T> key) {
        return getComponent(new ComponentKey<>(key));
    }

    @Override
    public <T> T getComponent(final Class<T> key, final Annotation... qualifiers) {
        return getComponent(new ComponentKey<>(key, qualifiers));
    }

    @Override
    public void fire(final Object event) {
        definitions.fire(this, event);
    }

    @Override
    public void destroy() {
        fire(new ContainerDestroy());
    }
}
