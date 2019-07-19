package nablarch.fw.dicontainer;

import static org.junit.Assert.*;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Named;

import org.junit.Test;

import nablarch.fw.dicontainer.component.ComponentKey;
import nablarch.fw.dicontainer.component.ComponentKey.AliasKey;

public class ComponentKeyTest {

    @Test
    public void aliasKeysEmpty() throws Exception {
        final ComponentKey<?> key = new ComponentKey<>(Aaa.class);
        final Set<AliasKey> aliasKeys = key.aliasKeys();
        assertTrue(aliasKeys.isEmpty());
    }

    @Test
    public void aliasKeys() throws Exception {
        final ComponentKey<?> key = new ComponentKey<>(Bbb4.class);
        final Set<AliasKey> aliasKeys = key.aliasKeys();
        final Set<AliasKey> expected = Stream.of(
                new ComponentKey<>(Bbb1.class).asAliasKey(),
                new ComponentKey<>(Bbb2.class).asAliasKey(),
                new ComponentKey<>(Bbb3.class).asAliasKey())
                .collect(Collectors.toSet());
        assertEquals(expected, aliasKeys);
    }

    @Test
    public void aliasKeysWithQualifier() throws Exception {
        final ComponentKey<?> key = new ComponentKey<>(Ccc2.class, new NamedImpl("foo"));
        final Set<AliasKey> aliasKeys = key.aliasKeys();
        final Set<AliasKey> expected = Stream.of(
                new ComponentKey<>(Ccc1.class, new NamedImpl("foo")).asAliasKey(),
                new ComponentKey<>(Ccc1.class).asAliasKey(),
                new ComponentKey<>(Ccc2.class).asAliasKey())
                .collect(Collectors.toSet());
        assertEquals(expected, aliasKeys);
    }

    static class Aaa {
    }

    interface Bbb1 {
    }

    interface Bbb2 extends Bbb1 {
    }

    static class Bbb3 implements Bbb2 {
    }

    static class Bbb4 extends Bbb3 {
    }

    interface Ccc1 {
    }

    @Named("foo")
    static class Ccc2 implements Ccc1 {
    }
}
