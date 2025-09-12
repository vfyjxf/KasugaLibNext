package lib.kasuga.registration.core;

import lib.kasuga.registration.Reg;

public interface IChildrenConfiguration<S> {
    S self();

    S addChild(Reg<?, ?> child);

    Iterable<Reg<?, ?>> getChildren();
}
