package lib.kasuga.rendering.models.uml.dynamic.fsm;

import java.util.Set;

/** Restricts a layer's bone-channel influence to a named subset (or all bones). */
public record BoneMask(Set<String> names, boolean wildcard) {

    public static BoneMask all() {
        return new BoneMask(Set.of(), true);
    }

    public static BoneMask only(String... names) {
        return new BoneMask(Set.of(names), false);
    }

    public BoneMask {
        names = names == null ? Set.of() : Set.copyOf(names);
    }

    public boolean matches(String bone) {
        return wildcard || names.contains(bone);
    }
}
