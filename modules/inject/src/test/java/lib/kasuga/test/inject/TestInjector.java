package lib.kasuga.test.inject;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jdk.jfr.Name;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Optional;

@MicronautTest()
@Disabled("Requires Micronaut annotation processing + proper bean context")
public class TestInjector {
    @Inject @Named("mustInject")
    Optional<TestBean> injected;

    @Inject @Named("mustNotInject")
    Optional<TestBean> notInjected;

    @Test
    public void shouldStateCorrect(){
        Assertions.assertTrue(injected.isPresent(), "Injected bean must be present");
        Assertions.assertFalse(notInjected.isPresent(), "Not injected bean must not be present");
    }
}
