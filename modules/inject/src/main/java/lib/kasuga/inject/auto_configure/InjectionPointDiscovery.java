package lib.kasuga.inject.auto_configure;

import java.util.ArrayList;
import java.util.List;

public class InjectionPointDiscovery {
    List<InjectPointMatcher<?>> injectPointMatchers = new ArrayList<>();

    public void addMatcher() {}

    public InjectPointDiscoveryResult discovery() {
        return new InjectPointDiscoveryResult();
    }
}
