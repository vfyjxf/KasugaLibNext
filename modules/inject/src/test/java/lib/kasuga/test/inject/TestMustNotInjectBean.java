package lib.kasuga.test.inject;


import jakarta.inject.Named;
import jakarta.inject.Singleton;
import lib.kasuga.inject.BeanOnlyIn;

@Singleton
@Named("mustNotInject")
@BeanOnlyIn.Never
public class TestMustNotInjectBean implements TestBean {
    static {
        TestFlags.notInjectBeanLoaded = true;
    }
}
