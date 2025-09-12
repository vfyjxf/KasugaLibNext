package lib.kasuga.test.inject;

import io.micronaut.context.annotation.Bean;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Singleton
@Named("mustInject")
public class TestMustInjectBean implements TestBean {
    TestMustInjectBean(){}

    boolean test(){return true; }
}
