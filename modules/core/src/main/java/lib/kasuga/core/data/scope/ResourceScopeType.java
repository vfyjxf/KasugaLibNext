package lib.kasuga.core.data.scope;

import lib.kasuga.core.data.loader.PackResourceAccess;

import java.util.function.Function;

public class ResourceScopeType<T extends ResourceScope> {
    public final String scopeName;
    private final Function<PackResourceAccess, T> factory;

    public ResourceScopeType(String scopeName, Function<PackResourceAccess, T> factory) {
        this.scopeName = scopeName;
        this.factory = factory;
    }

    public String getScopeName() {
        return scopeName;
    }

    public T create(PackResourceAccess access) {
        return this.factory.apply(access);
    }
}
