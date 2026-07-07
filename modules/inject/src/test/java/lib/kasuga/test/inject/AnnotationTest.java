package lib.kasuga.test.inject;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.exceptions.NoSuchBeanException;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

@MicronautTest
@EnabledIfSystemProperty(named = "kasuga.integration.tests", matches = "true")
public class AnnotationTest {
    @Inject ApplicationContext context;

    @Test
    public void shouldBeanScanningWorks() {
        Assertions.assertDoesNotThrow(()->{
            TestBean testBean = context.getBean(TestBean.class, Qualifiers.byName("mustInject"));
            Assertions.assertNotNull(testBean);
        });

        Assertions.assertThrows(NoSuchBeanException.class, ()->{
            TestBean testBean = context.getBean(TestBean.class, Qualifiers.byName("mustNotInject"));
        });
    }

    @Test
    public void shouldNotInjectBeanClassNotLoaded(){
        Assertions.assertFalse(TestFlags.notInjectBeanLoaded);
    }
}
